package io.github.kk01001.redisson.example.controller;

import io.github.kk01001.redisson.holder.RedissonClientHolder;
import io.github.kk01001.redisson.monitor.DualWriteMetrics;
import io.github.kk01001.redisson.template.MultiRedissonTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * @author kk01001
 * @date 2026-01-15 17:00:00
 * @description Redisson 测试接口
 */
@Slf4j
@RestController
@RequestMapping("/redis")
@RequiredArgsConstructor
public class RedissonTestController {

    private final MultiRedissonTemplate redissonTemplate;
    private final RedissonClientHolder clientHolder;
    private final RedissonClient redissonClient;

    // ==================== String 操作测试 ====================

    /**
     * 设置值（双写）
     */
    @PostMapping("/string/set")
    public Map<String, Object> setString(@RequestParam String key, @RequestParam String value) {
        redissonTemplate.set(key, value, Duration.ofMinutes(10));
        
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("key", key);
        result.put("value", value);
        result.put("message", "Value set to both clusters");
        return result;
    }

    /**
     * 获取值（只从主集群读取）
     */
    @GetMapping("/string/get")
    public Map<String, Object> getString(@RequestParam String key) {
        String value = redissonTemplate.get(key);
        
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("key", key);
        result.put("value", value);
        result.put("exists", value != null);
        return result;
    }

    /**
     * 删除值（双写）
     */
    @DeleteMapping("/string/delete")
    public Map<String, Object> deleteString(@RequestParam String key) {
        boolean deleted = redissonTemplate.delete(key);
        
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", deleted);
        result.put("key", key);
        result.put("message", "Key deleted from both clusters");
        return result;
    }

    // ==================== 计数器测试 ====================

    /**
     * 递增（双写）
     */
    @PostMapping("/counter/incr")
    public Map<String, Object> increment(@RequestParam String key,
                                          @RequestParam(defaultValue = "1") long delta) {
        long value = redissonTemplate.increment(key, delta, Duration.ofHours(1));
        
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("key", key);
        result.put("delta", delta);
        result.put("currentValue", value);
        return result;
    }

    /**
     * 递减（双写）
     */
    @PostMapping("/counter/decr")
    public Map<String, Object> decrement(@RequestParam String key,
                                          @RequestParam(defaultValue = "1") long delta) {
        long value = redissonTemplate.decrement(key, delta, Duration.ofHours(1));
        
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("key", key);
        result.put("delta", delta);
        result.put("currentValue", value);
        return result;
    }

    // ==================== Set 操作测试 ====================

    /**
     * 添加 Set 元素（双写）
     */
    @PostMapping("/set/add")
    public Map<String, Object> setAdd(@RequestParam String key, @RequestParam String value) {
        redissonTemplate.sadd(key, value);
        
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("key", key);
        result.put("value", value);
        return result;
    }

    /**
     * 获取 Set 所有元素
     */
    @GetMapping("/set/members")
    public Map<String, Object> setMembers(@RequestParam String key) {
        Set<String> members = redissonTemplate.smembers(key);
        
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("key", key);
        result.put("members", members);
        result.put("size", members != null ? members.size() : 0);
        return result;
    }

    /**
     * 随机弹出元素（主集群随机弹出，备份集群删除指定元素）
     */
    @PostMapping("/set/pop")
    public Map<String, Object> setPop(@RequestParam String key) {
        String popped = redissonTemplate.spop(key);
        
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("key", key);
        result.put("poppedValue", popped);
        result.put("message", "Primary: random pop, Secondary: remove specific element");
        return result;
    }

    // ==================== Hash 操作测试 ====================

    /**
     * 设置 Hash 字段（双写）
     */
    @PostMapping("/hash/set")
    public Map<String, Object> hashSet(@RequestParam String key,
                                        @RequestParam String field,
                                        @RequestParam String value) {
        redissonTemplate.hset(key, field, value);
        
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("key", key);
        result.put("field", field);
        result.put("value", value);
        return result;
    }

    /**
     * 获取 Hash 字段
     */
    @GetMapping("/hash/get")
    public Map<String, Object> hashGet(@RequestParam String key, @RequestParam String field) {
        String value = redissonTemplate.hget(key, field);
        
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("key", key);
        result.put("field", field);
        result.put("value", value);
        return result;
    }

    /**
     * 获取 Hash 所有字段
     */
    @GetMapping("/hash/getAll")
    public Map<String, Object> hashGetAll(@RequestParam String key) {
        Map<String, String> data = redissonTemplate.hgetAll(key);
        
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("key", key);
        result.put("data", data);
        result.put("size", data != null ? data.size() : 0);
        return result;
    }

    // ==================== ZSet 操作测试 ====================

    /**
     * 添加 ZSet 元素（双写）
     */
    @PostMapping("/zset/add")
    public Map<String, Object> zsetAdd(@RequestParam String key,
                                        @RequestParam String value,
                                        @RequestParam double score) {
        redissonTemplate.zadd(key, value, score);
        
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("key", key);
        result.put("value", value);
        result.put("score", score);
        return result;
    }

    /**
     * 弹出最高分元素（主集群弹出，备份集群删除指定元素）
     */
    @PostMapping("/zset/popMax")
    public Map<String, Object> zsetPopMax(@RequestParam String key) {
        String popped = redissonTemplate.zpopMax(key);
        
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("key", key);
        result.put("poppedValue", popped);
        result.put("message", "Primary: pop max, Secondary: remove specific element");
        return result;
    }

    // ==================== 批量测试 ====================

    /**
     * 批量写入测试
     */
    @PostMapping("/batch/write")
    public Map<String, Object> batchWrite(@RequestParam(defaultValue = "100") int count) {
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < count; i++) {
            String key = "batch:test:" + i;
            String value = UUID.randomUUID().toString();
            redissonTemplate.set(key, value, Duration.ofMinutes(5));
        }
        
        long endTime = System.currentTimeMillis();
        
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("count", count);
        result.put("timeMs", endTime - startTime);
        result.put("avgTimeMs", (double)(endTime - startTime) / count);
        return result;
    }

    // ==================== 集群信息 ====================

    /**
     * 获取所有集群信息
     */
    @GetMapping("/clusters")
    public Map<String, Object> getClusters() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("clientNames", clientHolder.getClientNames());
        result.put("primaryName", clientHolder.getPrimaryName());
        result.put("dualWriteEnabled", redissonTemplate.isDualWriteEnabled());
        return result;
    }

    /**
     * 直接从指定集群读取（绕过双写模板）
     */
    @GetMapping("/cluster/{name}/get")
    public Map<String, Object> getFromCluster(@PathVariable String name, @RequestParam String key) {
        RedissonClient client = clientHolder.getClient(name);
        RBucket<String> bucket = client.getBucket(key);
        String value = bucket.get();
        
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("cluster", name);
        result.put("key", key);
        result.put("value", value);
        return result;
    }

    /**
     * 对比两个集群的值
     */
    @GetMapping("/compare")
    public Map<String, Object> compareValue(@RequestParam String key) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("key", key);
        
        Map<String, String> values = new HashMap<>();
        for (String name : clientHolder.getClientNames()) {
            RedissonClient client = clientHolder.getClient(name);
            RBucket<String> bucket = client.getBucket(key);
            values.put(name, bucket.get());
        }
        result.put("values", values);
        
        // 检查是否一致
        boolean consistent = values.values().stream().distinct().count() <= 1;
        result.put("consistent", consistent);
        
        return result;
    }

    // ==================== 监控信息 ====================

    /**
     * 获取双写监控指标
     */
    @GetMapping("/metrics")
    public Map<String, Object> getMetrics() {
        DualWriteMetrics metrics = redissonTemplate.getMetrics();
        
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalSubmitted", metrics.getTotalSubmitted());
        result.put("totalExecuted", metrics.getTotalExecuted());
        result.put("totalSuccess", metrics.getTotalSuccess());
        result.put("totalFailure", metrics.getTotalFailure());
        result.put("successRate", String.format("%.2f%%", metrics.getSuccessRate()));
        result.put("failureRate", String.format("%.2f%%", metrics.getFailureRate()));
        result.put("operationSuccess", metrics.getOperationSuccessCount());
        result.put("operationFailure", metrics.getOperationFailureCount());
        
        DualWriteMetrics.ThreadPoolStatus poolStatus = metrics.getThreadPoolStatus();
        if (poolStatus != null) {
            Map<String, Object> threadPool = new LinkedHashMap<>();
            threadPool.put("activeCount", poolStatus.activeCount());
            threadPool.put("poolSize", poolStatus.currentPoolSize());
            threadPool.put("queueSize", poolStatus.queueSize());
            threadPool.put("completedTaskCount", poolStatus.completedTaskCount());
            result.put("threadPool", threadPool);
        }
        
        return result;
    }
}
