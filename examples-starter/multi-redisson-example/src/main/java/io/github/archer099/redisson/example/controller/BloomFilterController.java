package io.github.archer099.redisson.example.controller;

import io.github.archer099.redisson.template.MultiRedissonTemplate;
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
 * @author archer099
 * @date 2026-01-17 11:00:00
 * @description 布隆过滤器 Demo - 缓存穿透防护实战案例
 */
@RestController
@RequestMapping("/api/bloom")
public class BloomFilterController {

    private static final String BLOOM_FILTER_KEY = "demo:bloom:products";
    private static final String CACHE_KEY = "demo:cache:product:";

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

    public BloomFilterController(MultiRedissonTemplate redissonTemplate) {
        this.redissonTemplate = redissonTemplate;
    }

    /**
     * 初始化布隆过滤器
     *
     * @param expectedInsertions 预期插入数量
     * @param falseProbability   误判率
     */
    @PostMapping("/init")
    public Map<String, Object> initBloomFilter(
            @RequestParam(defaultValue = "10000") long expectedInsertions,
            @RequestParam(defaultValue = "0.01") double falseProbability) {

        Map<String, Object> result = new LinkedHashMap<>();

        // 删除旧的布隆过滤器
        redissonTemplate.delete(BLOOM_FILTER_KEY);

        // 创建新的布隆过滤器
        boolean created = redissonTemplate.createBloomFilter(BLOOM_FILTER_KEY, expectedInsertions, falseProbability);

        // 将已存在的商品ID加入布隆过滤器
        int addedCount = 0;
        for (String productId : EXISTING_PRODUCTS) {
            if (redissonTemplate.addToBloomFilter(BLOOM_FILTER_KEY, productId)) {
                addedCount++;
            }
        }

        // 重置统计
        cacheHits.set(0);
        cacheMiss.set(0);
        dbQueries.set(0);
        bloomBlocked.set(0);

        result.put("success", created);
        result.put("expectedInsertions", expectedInsertions);
        result.put("falseProbability", falseProbability);
        result.put("addedProductCount", addedCount);
        result.put("message", created ? "布隆过滤器初始化成功" : "布隆过滤器初始化失败");

        return result;
    }

    /**
     * 获取布隆过滤器信息
     */
    @GetMapping("/info")
    public Map<String, Object> getBloomInfo() {
        Map<String, Object> result = new LinkedHashMap<>();

        result.put("exists", redissonTemplate.exists(BLOOM_FILTER_KEY));
        result.put("existingProducts", EXISTING_PRODUCTS);
        result.put("stats", Map.of(
                "cacheHits", cacheHits.get(),
                "cacheMiss", cacheMiss.get(),
                "dbQueries", dbQueries.get(),
                "bloomBlocked", bloomBlocked.get(),
                "hitRate", calculateHitRate()
        ));

        return result;
    }

    /**
     * 查询商品（使用布隆过滤器）
     */
    @GetMapping("/product")
    public Map<String, Object> getProductWithBloom(@RequestParam String productId) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("productId", productId);
        result.put("useBloomFilter", true);

        // 1. 先查缓存
        String cachedProduct = redissonTemplate.get(CACHE_KEY + productId);
        if (cachedProduct != null) {
            cacheHits.incrementAndGet();
            result.put("source", "CACHE");
            result.put("data", cachedProduct);
            return result;
        }
        cacheMiss.incrementAndGet();

        // 2. 布隆过滤器检查
        boolean mightExist = redissonTemplate.mightContainInBloomFilter(BLOOM_FILTER_KEY, productId);

        if (!mightExist) {
            // 布隆过滤器判断不存在，直接返回（100%准确）
            bloomBlocked.incrementAndGet();
            result.put("source", "BLOOM_BLOCKED");
            result.put("exists", false);
            result.put("message", "商品不存在（布隆过滤器拦截）");
            return result;
        }

        // 3. 查询数据库（模拟）
        dbQueries.incrementAndGet();
        String productData = queryFromDatabase(productId);

        if (productData != null) {
            // 写入缓存
            redissonTemplate.set(CACHE_KEY + productId, productData);
            result.put("source", "DATABASE");
            result.put("data", productData);
        } else {
            // 数据库也不存在（布隆过滤器误判）
            result.put("source", "DATABASE");
            result.put("exists", false);
            result.put("message", "商品不存在（布隆过滤器误判，查询了数据库）");
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
        result.put("useBloomFilter", false);

        // 1. 先查缓存
        String cachedProduct = redissonTemplate.get(CACHE_KEY + productId);
        if (cachedProduct != null) {
            cacheHits.incrementAndGet();
            result.put("source", "CACHE");
            result.put("data", cachedProduct);
            return result;
        }
        cacheMiss.incrementAndGet();

        // 2. 直接查询数据库（没有布隆过滤器保护）
        dbQueries.incrementAndGet();
        String productData = queryFromDatabase(productId);

        if (productData != null) {
            // 写入缓存
            redissonTemplate.set(CACHE_KEY + productId, productData);
            result.put("source", "DATABASE");
            result.put("data", productData);
        } else {
            // 不存在也查询了数据库（缓存穿透！）
            result.put("source", "DATABASE");
            result.put("exists", false);
            result.put("message", "商品不存在（直接查询数据库，可能造成缓存穿透）");
        }

        return result;
    }

    /**
     * 添加商品到布隆过滤器
     */
    @PostMapping("/add")
    public Map<String, Object> addToBloom(@RequestParam String productId) {
        Map<String, Object> result = new LinkedHashMap<>();

        boolean added = redissonTemplate.addToBloomFilter(BLOOM_FILTER_KEY, productId);

        result.put("success", true);
        result.put("productId", productId);
        result.put("newlyAdded", added);
        result.put("message", added ? "商品已添加到布隆过滤器" : "商品已存在于布隆过滤器中");

        return result;
    }

    /**
     * 批量检查商品是否存在
     */
    @GetMapping("/check-batch")
    public Map<String, Object> batchCheck(@RequestParam(defaultValue = "10") int count) {
        Map<String, Object> result = new LinkedHashMap<>();

        // 生成测试商品ID（一半存在，一半不存在）
        String[] testIds = new String[count];
        for (int i = 0; i < count; i++) {
            if (i % 2 == 0) {
                // 存在的商品
                testIds[i] = EXISTING_PRODUCTS[i % EXISTING_PRODUCTS.length];
            } else {
                // 不存在的商品
                testIds[i] = "FAKE_" + new Random().nextInt(10000);
            }
        }

        // 批量检查
        Map<String, Boolean> checkResults = new LinkedHashMap<>();
        int existsCount = 0;
        int notExistsCount = 0;

        for (String id : testIds) {
            boolean mightExist = redissonTemplate.mightContainInBloomFilter(BLOOM_FILTER_KEY, id);
            checkResults.put(id, mightExist);
            if (mightExist) {
                existsCount++;
            } else {
                notExistsCount++;
            }
        }

        result.put("total", count);
        result.put("mightExist", existsCount);
        result.put("notExist", notExistsCount);
        result.put("results", checkResults);

        return result;
    }

    /**
     * 重置统计
     */
    @PostMapping("/reset-stats")
    public Map<String, Object> resetStats() {
        Map<String, Object> result = new LinkedHashMap<>();

        cacheHits.set(0);
        cacheMiss.set(0);
        dbQueries.set(0);
        bloomBlocked.set(0);

        result.put("success", true);
        result.put("message", "统计已重置");

        return result;
    }

    /**
     * 模拟数据库查询
     */
    private String queryFromDatabase(String productId) {
        // 模拟数据库延迟
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 检查是否存在
        for (String existingId : EXISTING_PRODUCTS) {
            if (existingId.equals(productId)) {
                return String.format("{\"id\":\"%s\",\"name\":\"商品%s\",\"price\":%d}",
                        productId, productId, new Random().nextInt(1000) + 100);
            }
        }
        return null;
    }

    /**
     * 计算缓存命中率
     */
    private String calculateHitRate() {
        long total = cacheHits.get() + cacheMiss.get();
        if (total == 0) {
            return "N/A";
        }
        return String.format("%.2f%%", (double) cacheHits.get() / total * 100);
    }
}
