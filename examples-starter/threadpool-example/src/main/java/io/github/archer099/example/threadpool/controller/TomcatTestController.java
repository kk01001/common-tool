package io.github.archer099.example.threadpool.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tomcat 线程池测试控制器
 * 
 * @author archer099
 */
@Slf4j
@RestController
@RequestMapping("/api/tomcat")
public class TomcatTestController {

    private final AtomicInteger requestCounter = new AtomicInteger(0);

    /**
     * 单次测试请求
     * 模拟一个耗时操作，用于测试 Tomcat 线程池
     */
    @GetMapping("/test")
    public Map<String, Object> testRequest() {
        int requestId = requestCounter.incrementAndGet();
        String threadName = Thread.currentThread().getName();
        
        log.info("处理测试请求 #{} on thread: {}", requestId, threadName);
        
        try {
            // 模拟耗时操作
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("requestId", requestId);
        result.put("threadName", threadName);
        result.put("message", "请求处理成功");
        result.put("timestamp", System.currentTimeMillis());
        
        return result;
    }

    /**
     * 压力测试
     * 
     * @param count 请求数量
     * @param concurrency 并发数
     */
    @PostMapping("/stress")
    public Map<String, Object> stressTest(
            @RequestParam(defaultValue = "100") int count,
            @RequestParam(defaultValue = "10") int concurrency) {
        
        log.info("开始压力测试: count={}, concurrency={}", count, concurrency);
        
        long startTime = System.currentTimeMillis();
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        
        // 创建异步任务
        CompletableFuture<?>[] futures = new CompletableFuture[count];
        
        for (int i = 0; i < count; i++) {
            final int requestId = i;
            futures[i] = CompletableFuture.runAsync(() -> {
                try {
                    // 模拟请求处理
                    Thread.sleep(50 + (int)(Math.random() * 100));
                    successCount.incrementAndGet();
                    
                    if (requestId % 10 == 0) {
                        log.info("压测进度: {}/{}", requestId, count);
                    }
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    log.error("请求失败: {}", e.getMessage());
                }
            });
            
            // 控制并发数
            if ((i + 1) % concurrency == 0) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        
        // 等待所有任务完成
        CompletableFuture.allOf(futures).join();
        
        long duration = System.currentTimeMillis() - startTime;
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("totalRequests", count);
        result.put("successCount", successCount.get());
        result.put("failCount", failCount.get());
        result.put("duration", duration + "ms");
        result.put("qps", count * 1000.0 / duration);
        result.put("message", String.format("压测完成: 成功 %d, 失败 %d, 耗时 %dms, QPS %.2f",
                successCount.get(), failCount.get(), duration, count * 1000.0 / duration));
        
        log.info("压力测试完成: {}", result.get("message"));
        
        return result;
    }

    /**
     * 模拟慢请求
     */
    @GetMapping("/slow")
    public Map<String, Object> slowRequest(@RequestParam(defaultValue = "5000") long sleepMs) {
        String threadName = Thread.currentThread().getName();
        log.info("处理慢请求 on thread: {}, sleep={}ms", threadName, sleepMs);
        
        try {
            Thread.sleep(sleepMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("threadName", threadName);
        result.put("sleepMs", sleepMs);
        result.put("message", "慢请求处理完成");
        
        return result;
    }

    /**
     * 获取当前请求计数
     */
    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRequests", requestCounter.get());
        stats.put("currentThread", Thread.currentThread().getName());
        stats.put("timestamp", System.currentTimeMillis());
        
        return stats;
    }
}
