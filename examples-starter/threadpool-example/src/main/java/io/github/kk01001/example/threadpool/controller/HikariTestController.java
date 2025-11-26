package io.github.kk01001.example.threadpool.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Hikari 连接池测试控制器
 * 
 * @author kk01001
 */
@Slf4j
@RestController
@RequestMapping("/api/hikari")
@RequiredArgsConstructor
public class HikariTestController {

    private final JdbcTemplate jdbcTemplate;
    private final AtomicInteger requestCounter = new AtomicInteger(0);

    /**
     * 单次数据库查询测试
     */
    @GetMapping("/test")
    public Map<String, Object> testRequest() {
        int requestId = requestCounter.incrementAndGet();
        String threadName = Thread.currentThread().getName();

        log.info("处理数据库测试请求 #{} on thread: {}", requestId, threadName);

        try {
            // 执行简单查询
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("requestId", requestId);
            response.put("threadName", threadName);
            response.put("queryResult", result);
            response.put("message", "数据库查询成功");
            response.put("timestamp", System.currentTimeMillis());

            return response;
        } catch (Exception e) {
            log.error("数据库查询失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("requestId", requestId);
            response.put("error", e.getMessage());
            return response;
        }
    }

    /**
     * 数据库连接压力测试
     * 
     * @param count       请求数量
     * @param concurrency 并发数
     */
    @PostMapping("/stress")
    public Map<String, Object> stressTest(
            @RequestParam(defaultValue = "100") int count,
            @RequestParam(defaultValue = "10") int concurrency) {

        log.info("开始数据库连接压力测试: count={}, concurrency={}", count, concurrency);

        long startTime = System.currentTimeMillis();
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // 创建异步任务
        CompletableFuture<?>[] futures = new CompletableFuture[count];

        for (int i = 0; i < count; i++) {
            final int requestId = i;
            futures[i] = CompletableFuture.runAsync(() -> {
                try {
                    // 模拟数据库查询
                    jdbcTemplate.queryForObject("SELECT 1", Integer.class);

                    // 模拟一些处理时间
                    Thread.sleep(50 + (int) (Math.random() * 100));
                    successCount.incrementAndGet();

                    if (requestId % 10 == 0) {
                        log.info("压测进度: {}/{}", requestId, count);
                    }
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    log.error("数据库查询失败: {}", e.getMessage());
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

        log.info("数据库连接压力测试完成: {}", result.get("message"));

        return result;
    }

    /**
     * 模拟慢查询
     */
    @GetMapping("/slow")
    public Map<String, Object> slowRequest(@RequestParam(defaultValue = "5000") long sleepMs) {
        String threadName = Thread.currentThread().getName();
        log.info("处理慢查询 on thread: {}, sleep={}ms", threadName, sleepMs);

        try {
            // 执行查询
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);

            // 模拟慢查询
            Thread.sleep(sleepMs);
        } catch (Exception e) {
            log.error("慢查询失败", e);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("threadName", threadName);
        result.put("sleepMs", sleepMs);
        result.put("message", "慢查询处理完成");

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

    /**
     * 初始化数据库表(用于测试)
     */
    @PostMapping("/init")
    public Map<String, Object> initDatabase() {
        try {
            // 创建测试表
            jdbcTemplate.execute("DROP TABLE IF EXISTS test_table");
            jdbcTemplate.execute(
                    "CREATE TABLE test_table (" +
                            "id INT PRIMARY KEY AUTO_INCREMENT, " +
                            "name VARCHAR(100), " +
                            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            // 插入一些测试数据
            for (int i = 1; i <= 10; i++) {
                jdbcTemplate.update("INSERT INTO test_table (name) VALUES (?)", "Test-" + i);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "数据库初始化成功");
            result.put("recordsInserted", 10);

            return result;
        } catch (Exception e) {
            log.error("数据库初始化失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", e.getMessage());
            return result;
        }
    }
}
