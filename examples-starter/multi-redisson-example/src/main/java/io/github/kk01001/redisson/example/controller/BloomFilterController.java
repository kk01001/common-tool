package io.github.kk01001.redisson.example.controller;

import io.github.kk01001.redisson.template.MultiRedissonTemplate;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author kk01001
 * @date 2026-01-17 11:00:00
 * @description 布隆过滤器 Demo - 缓存穿透防护实战案例
 */
@RestController
@RequestMapping("/api/bloom")
public class BloomFilterController {

    private static final String BLOOM_FILTER_KEY = "demo:bloom:";
    private static final String CACHE_KEY = "demo:cache:";

    private final RedissonClient redissonClient;
    private final MultiRedissonTemplate redissonTemplate;

    /**
     * 模拟数据库（实际存在的商品ID）
     */
    private static final String[] EXISTING_PRODUCTS = {
            "P001", "P002", "P003", "P004", "P005",
            "P006", "P007", "P008", "P009", "P010"
    };

    /**
     * 统计
     */
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMiss = new AtomicLong(0);
    private final AtomicLong dbQueries = new AtomicLong(0);
    private final AtomicLong bloomBlocked = new AtomicLong(0);

    public BloomFilterController(RedissonClient redissonClient, MultiRedissonTemplate redissonTemplate) {
        this.redissonClient = redissonClient;
        this.redissonTemplate = redissonTemplate;
    }

    /**
     * 初始化布隆过滤器
     *
     * @param expectedSize 预期元素数量
     * @param falseProbability 误判率
     */
    @PostMapping("/init")
    public Map<String, Object> initBloomFilter(
            @RequestParam(defaultValue = "products") String name,
            @RequestParam(defaultValue = "10000") long expectedSize,
            @RequestParam(defaultValue = "0.01") double falseProbability) {
        
        Map<String, Object> result = new LinkedHashMap<>();
        
        RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter(BLOOM_FILTER_KEY + name);
        
        // 删除旧数据
        bloomFilter.delete();
        
        // 初始化布隆过滤器
        boolean success = bloomFilter.tryInit(expectedSize, falseProbability);
        
        // 将已存在的商品ID加入布隆过滤器
        for (String productId : EXISTING_PRODUCTS) {
            bloomFilter.add(productId);
        }
        
        // 重置统计
        cacheHits.set(0);
        cacheMiss.set(0);
        dbQueries.set(0);
        bloomBlocked.set(0);
        
        result.put("success", success);
        result.put("name", name);
        result.put("expectedSize", expectedSize);
        result.put("falseProbability", falseProbability);
        result.put("addedProducts", EXISTING_PRODUCTS.length);
        result.put("existingProducts", EXISTING_PRODUCTS);
        result.put("message", "布隆过滤器初始化完成，已添加 " + EXISTING_PRODUCTS.length + " 个商品ID");
        
        return result;
    }

    /**
     * 获取布隆过滤器信息
     */
    @GetMapping("/info")
    public Map<String, Object> getInfo(@RequestParam(defaultValue = "products") String name) {
        Map<String, Object> result = new LinkedHashMap<>();
        
        RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter(BLOOM_FILTER_KEY + name);
        
        if (!bloomFilter.isExists()) {
            result.put("exists", false);
            result.put("message", "布隆过滤器不存在，请先初始化");
            return result;
        }
        
        result.put("exists", true);
        result.put("name", name);
        result.put("count", bloomFilter.count());
        result.put("expectedInsertions", bloomFilter.getExpectedInsertions());
        result.put("falseProbability", bloomFilter.getFalseProbability());
        result.put("hashIterations", bloomFilter.getHashIterations());
        result.put("size", bloomFilter.getSize());
        
        // 统计信息
        result.put("stats", Map.of(
                "cacheHits", cacheHits.get(),
                "cacheMiss", cacheMiss.get(),
                "dbQueries", dbQueries.get(),
                "bloomBlocked", bloomBlocked.get(),
                "blockRate", calculateBlockRate()
        ));
        
        return result;
    }

    /**
     * 查询商品（使用布隆过滤器防止缓存穿透）
     */
    @GetMapping("/product")
    public Map<String, Object> getProductWithBloom(
            @RequestParam String productId,
            @RequestParam(defaultValue = "products") String bloomName) {
        
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("productId", productId);
        
        RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter(BLOOM_FILTER_KEY + bloomName);
        
        // 1. 先查布隆过滤器
        if (!bloomFilter.contains(productId)) {
            // 布隆过滤器判断不存在，直接返回（100%准确）
            bloomBlocked.incrementAndGet();
            result.put("source", "BLOOM_FILTER");
            result.put("exists", false);
            result.put("message", "商品不存在（布隆过滤器拦截，避免穿透到数据库）");
            result.put("dbQueryAvoided", true);
            return result;
        }
        
        // 2. 布隆过滤器判断可能存在，查缓存
        String cacheKey = CACHE_KEY + productId;
        String cachedData = redissonTemplate.get(cacheKey);
        
        if (cachedData != null) {
            cacheHits.incrementAndGet();
            result.put("source", "CACHE");
            result.put("exists", true);
            result.put("data", cachedData);
            result.put("message", "从缓存获取");
            return result;
        }
        
        cacheMiss.incrementAndGet();
        
        // 3. 缓存未命中，查数据库
        dbQueries.incrementAndGet();
        String dbData = queryFromDatabase(productId);
        
        if (dbData != null) {
            // 数据存在，写入缓存
            redissonTemplate.set(cacheKey, dbData, java.time.Duration.ofMinutes(10));
            result.put("source", "DATABASE");
            result.put("exists", true);
            result.put("data", dbData);
            result.put("message", "从数据库获取，已写入缓存");
        } else {
            // 数据不存在（布隆过滤器误判）
            result.put("source", "DATABASE");
            result.put("exists", false);
            result.put("message", "商品不存在（布隆过滤器误判，查询了数据库）");
            result.put("falsePositive", true);
        }
        
        return result;
    }

    /**
     * 查询商品（不使用布隆过滤器 - 对比演示）
     */
    @GetMapping("/product-no-bloom")
    public Map<String, Object> getProductWithoutBloom(@RequestParam String productId) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("productId", productId);
        
        // 直接查缓存
        String cacheKey = CACHE_KEY + "no-bloom:" + productId;
        String cachedData = redissonTemplate.get(cacheKey);
        
        if (cachedData != null) {
            result.put("source", "CACHE");
            result.put("exists", true);
            result.put("data", cachedData);
            return result;
        }
        
        // 缓存未命中，直接查数据库（可能被恶意请求穿透！）
        dbQueries.incrementAndGet();
        String dbData = queryFromDatabase(productId);
        
        if (dbData != null) {
            redissonTemplate.set(cacheKey, dbData, java.time.Duration.ofMinutes(10));
            result.put("source", "DATABASE");
            result.put("exists", true);
            result.put("data", dbData);
        } else {
            result.put("source", "DATABASE");
            result.put("exists", false);
            result.put("message", "商品不存在，但已查询数据库（存在缓存穿透风险！）");
            result.put("warning", "⚠️ 无布隆过滤器保护，每次都会查询数据库");
        }
        
        return result;
    }

    /**
     * 添加商品到布隆过滤器
     */
    @PostMapping("/add")
    public Map<String, Object> addToBloom(
            @RequestParam String productId,
            @RequestParam(defaultValue = "products") String bloomName) {
        
        Map<String, Object> result = new LinkedHashMap<>();
        
        RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter(BLOOM_FILTER_KEY + bloomName);
        
        if (!bloomFilter.isExists()) {
            result.put("success", false);
            result.put("message", "布隆过滤器不存在");
            return result;
        }
        
        boolean added = bloomFilter.add(productId);
        
        result.put("success", true);
        result.put("productId", productId);
        result.put("newlyAdded", added);
        result.put("message", added ? "添加成功" : "元素可能已存在");
        result.put("currentCount", bloomFilter.count());
        
        return result;
    }

    /**
     * 批量检查（演示误判率）
     */
    @GetMapping("/batch-check")
    public Map<String, Object> batchCheck(
            @RequestParam(defaultValue = "products") String bloomName,
            @RequestParam(defaultValue = "100") int count) {
        
        Map<String, Object> result = new LinkedHashMap<>();
        
        RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter(BLOOM_FILTER_KEY + bloomName);
        
        if (!bloomFilter.isExists()) {
            result.put("success", false);
            result.put("message", "布隆过滤器不存在");
            return result;
        }
        
        Random random = new Random();
        int truePositive = 0;  // 正确判断存在
        int trueNegative = 0;  // 正确判断不存在
        int falsePositive = 0; // 误判（不存在判断为存在）
        
        for (int i = 0; i < count; i++) {
            String testId = "TEST_" + random.nextInt(10000);
            boolean bloomResult = bloomFilter.contains(testId);
            boolean actualExists = existsInDatabase(testId);
            
            if (bloomResult && actualExists) {
                truePositive++;
            } else if (!bloomResult && !actualExists) {
                trueNegative++;
            } else if (bloomResult && !actualExists) {
                falsePositive++;
            }
            // 注意：布隆过滤器不会有 falseNegative（不存在误判为存在不可能）
        }
        
        result.put("total", count);
        result.put("truePositive", truePositive);
        result.put("trueNegative", trueNegative);
        result.put("falsePositive", falsePositive);
        result.put("actualFalseProbability", String.format("%.4f", (double) falsePositive / count));
        result.put("expectedFalseProbability", bloomFilter.getFalseProbability());
        
        return result;
    }

    /**
     * 重置统计
     */
    @PostMapping("/reset-stats")
    public Map<String, Object> resetStats() {
        cacheHits.set(0);
        cacheMiss.set(0);
        dbQueries.set(0);
        bloomBlocked.set(0);
        
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("message", "统计已重置");
        return result;
    }

    /**
     * 模拟查询数据库
     */
    private String queryFromDatabase(String productId) {
        // 模拟数据库延迟
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        for (String id : EXISTING_PRODUCTS) {
            if (id.equals(productId)) {
                return String.format("{\"id\":\"%s\",\"name\":\"商品%s\",\"price\":%.2f}", 
                        id, id, new Random().nextDouble() * 1000);
            }
        }
        return null;
    }

    /**
     * 检查是否存在于数据库
     */
    private boolean existsInDatabase(String productId) {
        for (String id : EXISTING_PRODUCTS) {
            if (id.equals(productId)) {
                return true;
            }
        }
        return false;
    }

    private String calculateBlockRate() {
        long total = cacheHits.get() + cacheMiss.get() + bloomBlocked.get();
        if (total == 0) return "0%";
        return String.format("%.2f%%", (double) bloomBlocked.get() / total * 100);
    }
}
