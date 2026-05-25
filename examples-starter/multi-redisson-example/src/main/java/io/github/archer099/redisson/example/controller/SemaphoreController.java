package io.github.archer099.redisson.example.controller;

import io.github.archer099.redisson.template.MultiRedissonTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author archer099
 * @date 2026-01-17 19:30:00
 * @description 分布式信号量 Demo - 资源并发控制实战案例
 * <p>
 * 应用场景：
 * 1. 数据库连接池控制
 * 2. 停车场车位管理
 * 3. 会议室预约
 * 4. API 并发调用限制
 * </p>
 */
@RestController
@RequestMapping("/api/semaphore")
public class SemaphoreController {

    private static final String SEMAPHORE_KEY = "demo:semaphore:";
    private static final String PARKING_KEY = "demo:parking:";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private final MultiRedissonTemplate redissonTemplate;

    /**
     * 统计信息
     */
    private final AtomicLong acquireSuccess = new AtomicLong(0);
    private final AtomicLong acquireFailed = new AtomicLong(0);

    public SemaphoreController(MultiRedissonTemplate redissonTemplate) {
        this.redissonTemplate = redissonTemplate;
    }

    /**
     * 初始化信号量
     *
     * @param name    信号量名称
     * @param permits 许可数量
     */
    @PostMapping("/init")
    public Map<String, Object> initSemaphore(
            @RequestParam(defaultValue = "resource") String name,
            @RequestParam(defaultValue = "5") int permits) {

        Map<String, Object> result = new LinkedHashMap<>();

        String key = SEMAPHORE_KEY + name;
        redissonTemplate.trySetSemaphorePermits(key, permits);

        // 重置统计
        acquireSuccess.set(0);
        acquireFailed.set(0);

        result.put("success", true);
        result.put("name", name);
        result.put("totalPermits", permits);
        result.put("availablePermits", redissonTemplate.getSemaphoreAvailablePermits(key));
        result.put("message", String.format("信号量初始化成功，共 %d 个许可", permits));

        return result;
    }

    /**
     * 获取信号量状态
     */
    @GetMapping("/status")
    public Map<String, Object> getStatus(@RequestParam(defaultValue = "resource") String name) {
        Map<String, Object> result = new LinkedHashMap<>();

        String key = SEMAPHORE_KEY + name;

        result.put("name", name);
        result.put("availablePermits", redissonTemplate.getSemaphoreAvailablePermits(key));
        result.put("acquireSuccess", acquireSuccess.get());
        result.put("acquireFailed", acquireFailed.get());

        return result;
    }

    /**
     * 获取许可（模拟资源占用）
     *
     * @param name     信号量名称
     * @param holdTime 持有时间（秒）
     */
    @PostMapping("/acquire")
    public Map<String, Object> acquire(
            @RequestParam(defaultValue = "resource") String name,
            @RequestParam(defaultValue = "3") int holdTime) {

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("timestamp", LocalDateTime.now().format(FORMATTER));
        result.put("name", name);

        String key = SEMAPHORE_KEY + name;

        try {
            // 尝试在2秒内获取许可
            boolean acquired = redissonTemplate.tryAcquireSemaphore(key, 2, TimeUnit.SECONDS);

            if (acquired) {
                acquireSuccess.incrementAndGet();
                result.put("success", true);
                result.put("message", String.format("获取许可成功，将持有 %d 秒", holdTime));
                result.put("availablePermits", redissonTemplate.getSemaphoreAvailablePermits(key));

                // 异步释放许可（模拟资源使用后释放）
                String finalKey = key;
                new Thread(() -> {
                    try {
                        Thread.sleep(holdTime * 1000L);
                        redissonTemplate.releaseSemaphore(finalKey);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();

            } else {
                acquireFailed.incrementAndGet();
                result.put("success", false);
                result.put("message", "获取许可失败，资源已满");
                result.put("availablePermits", redissonTemplate.getSemaphoreAvailablePermits(key));
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            result.put("success", false);
            result.put("message", "操作被中断");
        }

        return result;
    }

    /**
     * 释放许可
     */
    @PostMapping("/release")
    public Map<String, Object> release(@RequestParam(defaultValue = "resource") String name) {
        Map<String, Object> result = new LinkedHashMap<>();

        String key = SEMAPHORE_KEY + name;
        redissonTemplate.releaseSemaphore(key);

        result.put("success", true);
        result.put("message", "许可已释放");
        result.put("availablePermits", redissonTemplate.getSemaphoreAvailablePermits(key));

        return result;
    }

    // ==================== 停车场场景 ====================

    /**
     * 初始化停车场
     *
     * @param spaces 车位数量
     */
    @PostMapping("/parking/init")
    public Map<String, Object> initParking(@RequestParam(defaultValue = "10") int spaces) {
        Map<String, Object> result = new LinkedHashMap<>();

        String key = PARKING_KEY + "spaces";
        redissonTemplate.trySetSemaphorePermits(key, spaces);

        result.put("success", true);
        result.put("totalSpaces", spaces);
        result.put("availableSpaces", redissonTemplate.getSemaphoreAvailablePermits(key));
        result.put("message", String.format("停车场初始化成功，共 %d 个车位", spaces));

        return result;
    }

    /**
     * 车辆入场
     */
    @PostMapping("/parking/enter")
    public Map<String, Object> parkingEnter(@RequestParam String carNumber) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("timestamp", LocalDateTime.now().format(FORMATTER));
        result.put("carNumber", carNumber);

        String key = PARKING_KEY + "spaces";

        // 尝试获取车位
        boolean hasSpace = redissonTemplate.tryAcquireSemaphore(key);

        if (hasSpace) {
            result.put("success", true);
            result.put("message", String.format("车辆 %s 入场成功", carNumber));
            result.put("availableSpaces", redissonTemplate.getSemaphoreAvailablePermits(key));
        } else {
            result.put("success", false);
            result.put("message", "车位已满，请稍后再试");
            result.put("availableSpaces", 0);
        }

        return result;
    }

    /**
     * 车辆出场
     */
    @PostMapping("/parking/exit")
    public Map<String, Object> parkingExit(@RequestParam String carNumber) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("timestamp", LocalDateTime.now().format(FORMATTER));
        result.put("carNumber", carNumber);

        String key = PARKING_KEY + "spaces";
        redissonTemplate.releaseSemaphore(key);

        result.put("success", true);
        result.put("message", String.format("车辆 %s 出场成功", carNumber));
        result.put("availableSpaces", redissonTemplate.getSemaphoreAvailablePermits(key));

        return result;
    }

    /**
     * 获取停车场状态
     */
    @GetMapping("/parking/status")
    public Map<String, Object> parkingStatus() {
        Map<String, Object> result = new LinkedHashMap<>();

        String key = PARKING_KEY + "spaces";
        int available = redissonTemplate.getSemaphoreAvailablePermits(key);

        result.put("availableSpaces", available);
        result.put("message", available > 0 ? "有空余车位" : "车位已满");

        return result;
    }

    // ==================== 可过期的信号量 ====================

    /**
     * 获取可过期的许可（自动释放）
     */
    @PostMapping("/expirable/acquire")
    public Map<String, Object> acquireExpirable(
            @RequestParam(defaultValue = "expirable") String name,
            @RequestParam(defaultValue = "10") int leaseTime) {

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("timestamp", LocalDateTime.now().format(FORMATTER));

        String key = SEMAPHORE_KEY + name;

        // 初始化（如果不存在）
        redissonTemplate.trySetExpirableSemaphorePermits(key, 5);

        try {
            // 获取可过期的许可，到期自动释放
            String permitId = redissonTemplate.tryAcquireExpirableSemaphore(key, 2, leaseTime, TimeUnit.SECONDS);

            if (permitId != null) {
                result.put("success", true);
                result.put("permitId", permitId);
                result.put("leaseTime", leaseTime);
                result.put("message", String.format("获取许可成功，%d 秒后自动释放", leaseTime));
            } else {
                result.put("success", false);
                result.put("message", "获取许可失败");
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            result.put("success", false);
            result.put("message", "操作被中断");
        }

        return result;
    }

    /**
     * 释放可过期的许可
     */
    @PostMapping("/expirable/release")
    public Map<String, Object> releaseExpirable(
            @RequestParam(defaultValue = "expirable") String name,
            @RequestParam String permitId) {

        Map<String, Object> result = new LinkedHashMap<>();

        String key = SEMAPHORE_KEY + name;

        try {
            redissonTemplate.releaseExpirableSemaphore(key, permitId);
            result.put("success", true);
            result.put("message", "许可已释放");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "释放失败：" + e.getMessage());
        }

        return result;
    }
}
