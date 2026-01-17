package io.github.kk01001.redisson.example.controller;

import io.github.kk01001.redisson.template.MultiRedissonTemplate;
import org.redisson.api.RateIntervalUnit;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author kk01001
 * @date 2026-01-17 10:30:00
 * @description 限流器 Demo - API限流实战案例
 */
@RestController
@RequestMapping("/api/ratelimiter")
public class RateLimiterController {

    private static final String RATE_LIMITER_KEY = "demo:ratelimiter:";

    private final MultiRedissonTemplate redissonTemplate;

    /**
     * 统计信息
     */
    private final Map<String, AtomicLong> successCount = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> rejectCount = new ConcurrentHashMap<>();

    public RateLimiterController(MultiRedissonTemplate redissonTemplate) {
        this.redissonTemplate = redissonTemplate;
    }

    /**
     * 初始化限流器
     *
     * @param name     限流器名称
     * @param rate     速率（每个时间单位允许的请求数）
     * @param interval 时间间隔
     * @param unit     时间单位（SECONDS/MINUTES/HOURS）
     */
    @PostMapping("/init")
    public Map<String, Object> initRateLimiter(
            @RequestParam(defaultValue = "api") String name,
            @RequestParam(defaultValue = "10") long rate,
            @RequestParam(defaultValue = "1") long interval,
            @RequestParam(defaultValue = "SECONDS") String unit) {

        Map<String, Object> result = new LinkedHashMap<>();

        String key = RATE_LIMITER_KEY + name;

        // 删除旧配置
        redissonTemplate.deleteRateLimiter(key);

        // 设置新配置
        RateIntervalUnit rateUnit = RateIntervalUnit.valueOf(unit.toUpperCase());
        boolean success = redissonTemplate.trySetRateLimiter(key, rate, interval, rateUnit);

        // 重置统计
        successCount.put(name, new AtomicLong(0));
        rejectCount.put(name, new AtomicLong(0));

        result.put("success", success);
        result.put("name", name);
        result.put("rate", rate);
        result.put("interval", interval);
        result.put("unit", unit);
        result.put("description", String.format("每 %d %s 允许 %d 个请求", interval, unit, rate));

        return result;
    }

    /**
     * 获取限流器配置
     */
    @GetMapping("/config")
    public Map<String, Object> getConfig(@RequestParam(defaultValue = "api") String name) {
        Map<String, Object> result = new LinkedHashMap<>();

        String key = RATE_LIMITER_KEY + name;

        if (!redissonTemplate.rateLimiterExists(key)) {
            result.put("exists", false);
            result.put("message", "限流器不存在，请先初始化");
            return result;
        }

        result.put("exists", true);
        result.put("name", name);
        result.put("availablePermits", redissonTemplate.getRateLimiterAvailablePermits(key));
        result.put("successCount", successCount.getOrDefault(name, new AtomicLong(0)).get());
        result.put("rejectCount", rejectCount.getOrDefault(name, new AtomicLong(0)).get());

        return result;
    }

    /**
     * 尝试获取许可
     */
    @PostMapping("/acquire")
    public Map<String, Object> tryAcquire(@RequestParam(defaultValue = "api") String name,
                                          @RequestParam(defaultValue = "1") int permits) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS")));
        result.put("name", name);
        result.put("requestedPermits", permits);

        String key = RATE_LIMITER_KEY + name;

        if (!redissonTemplate.rateLimiterExists(key)) {
            result.put("success", false);
            result.put("message", "限流器不存在，请先初始化");
            return result;
        }

        boolean acquired = redissonTemplate.tryAcquireRateLimiter(key, permits);

        if (acquired) {
            successCount.computeIfAbsent(name, k -> new AtomicLong(0)).incrementAndGet();
            result.put("success", true);
            result.put("message", "✅ 获取许可成功");
        } else {
            rejectCount.computeIfAbsent(name, k -> new AtomicLong(0)).incrementAndGet();
            result.put("success", false);
            result.put("message", "❌ 请求被限流，请稍后重试");
        }

        result.put("availablePermits", redissonTemplate.getRateLimiterAvailablePermits(key));
        result.put("totalSuccess", successCount.getOrDefault(name, new AtomicLong(0)).get());
        result.put("totalReject", rejectCount.getOrDefault(name, new AtomicLong(0)).get());

        return result;
    }

    /**
     * 模拟 API 调用（带限流）
     */
    @GetMapping("/api-call")
    public Map<String, Object> apiCall(@RequestParam(defaultValue = "api") String name,
                                       @RequestParam(defaultValue = "default") String userId) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS")));
        result.put("userId", userId);

        String key = RATE_LIMITER_KEY + name;

        if (!redissonTemplate.rateLimiterExists(key)) {
            // 默认初始化：每秒10个请求
            redissonTemplate.trySetRateLimiter(key, 10, 1, RateIntervalUnit.SECONDS);
        }

        if (redissonTemplate.tryAcquireRateLimiter(key)) {
            successCount.computeIfAbsent(name, k -> new AtomicLong(0)).incrementAndGet();

            // 模拟业务处理
            result.put("success", true);
            result.put("code", 200);
            result.put("message", "请求成功");
            result.put("data", Map.of(
                    "userId", userId,
                    "timestamp", System.currentTimeMillis(),
                    "info", "这是模拟的业务数据"
            ));
        } else {
            rejectCount.computeIfAbsent(name, k -> new AtomicLong(0)).incrementAndGet();

            result.put("success", false);
            result.put("code", 429);
            result.put("message", "请求过于频繁，请稍后重试");
        }

        return result;
    }

    /**
     * 用户级别限流
     */
    @PostMapping("/user-limit")
    public Map<String, Object> userRateLimit(@RequestParam String userId,
                                             @RequestParam(defaultValue = "5") long rate) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS")));
        result.put("userId", userId);

        String userLimiterKey = RATE_LIMITER_KEY + "user:" + userId;

        // 为每个用户设置独立限流：每秒 rate 个请求
        if (!redissonTemplate.rateLimiterExists(userLimiterKey)) {
            redissonTemplate.trySetRateLimiter(userLimiterKey, rate, 1, RateIntervalUnit.SECONDS);
            result.put("initialized", true);
        }

        if (redissonTemplate.tryAcquireRateLimiter(userLimiterKey)) {
            result.put("success", true);
            result.put("message", "请求成功");
        } else {
            result.put("success", false);
            result.put("message", String.format("用户 %s 请求过于频繁", userId));
            result.put("availablePermits", redissonTemplate.getRateLimiterAvailablePermits(userLimiterKey));
        }

        return result;
    }

    /**
     * 重置统计
     */
    @PostMapping("/reset-stats")
    public Map<String, Object> resetStats(@RequestParam(defaultValue = "api") String name) {
        Map<String, Object> result = new LinkedHashMap<>();

        successCount.put(name, new AtomicLong(0));
        rejectCount.put(name, new AtomicLong(0));

        result.put("success", true);
        result.put("message", "统计已重置");
        return result;
    }
}
