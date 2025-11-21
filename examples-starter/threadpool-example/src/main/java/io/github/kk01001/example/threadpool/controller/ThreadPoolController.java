package io.github.kk01001.example.threadpool.controller;

import com.alibaba.ttl.TransmittableThreadLocal;
import io.github.kk01001.threadpool.actuator.ThreadPoolMetrics;
import io.github.kk01001.threadpool.custom.wrapper.DynamicThreadPoolWrapper;
import io.github.kk01001.threadpool.registry.ThreadPoolRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程池测试控制器
 *
 * @author kk01001
 */
@RestController
@RequestMapping("/api/threadpool")
public class ThreadPoolController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ThreadPoolController.class);

    @Autowired
    private ThreadPoolRegistry registry;

    // @Resource(name = "demo-pool")
    // private Executor demoPool;

    private final AtomicInteger taskCounter = new AtomicInteger(0);

    // TTL 上下文
    private static final TransmittableThreadLocal<String> contextHolder = new TransmittableThreadLocal<>();

    /**
     * 提交任务
     */
    @PostMapping("/submit")
    public Map<String, Object> submitTask(@RequestParam(defaultValue = "1") int count,
                                          @RequestParam(defaultValue = "1000") long sleepMs,
                                          @RequestParam(defaultValue = "demo-pool") String poolName) {
        DynamicThreadPoolWrapper wrapper = registry.getThreadPool(poolName);
        if (wrapper == null) {
            return Map.of("success", false, "message", "Thread pool not found: " + poolName);
        }

        // 设置 TTL 上下文
        String requestId = "REQ-" + System.currentTimeMillis();
        contextHolder.set(requestId);

        int submitted = 0;
        for (int i = 0; i < count; i++) {
            int taskId = taskCounter.incrementAndGet();
            try {
                wrapper.execute(() -> {
                    try {
                        String ctx = contextHolder.get();
                        log.info("Task-{} started in pool [{}], Context: {}, Thread: {}",
                                taskId, poolName, ctx, Thread.currentThread().getName());
                        TimeUnit.MILLISECONDS.sleep(sleepMs);
                        log.info("Task-{} completed in pool [{}]", taskId, poolName);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.warn("Task-{} interrupted", taskId);
                    }
                });
                submitted++;
            } catch (Exception e) {
                log.error("Failed to submit task-{}", taskId, e);
            }
        }

        contextHolder.remove();

        return Map.of(
                "success", true,
                "submitted", submitted,
                "poolName", poolName,
                "sleepMs", sleepMs
        );
    }

    /**
     * 获取所有线程池指标
     */
    @GetMapping("/metrics")
    public Map<String, ThreadPoolMetrics> getAllMetrics() {
        return registry.collectAllMetrics();
    }

    /**
     * 获取指定线程池指标
     */
    @GetMapping("/metrics/{poolName}")
    public ThreadPoolMetrics getPoolMetrics(@PathVariable String poolName) {
        DynamicThreadPoolWrapper wrapper = registry.getThreadPool(poolName);
        if (wrapper == null) {
            throw new IllegalArgumentException("Thread pool not found: " + poolName);
        }
        return wrapper.collectMetrics();
    }

    /**
     * 获取所有线程池列表
     */
    @GetMapping("/pools")
    public Collection<String> getAllPools() {
        return registry.getAllPoolNames();
    }

    /**
     * 压力测试
     */
    @PostMapping("/stress")
    public Map<String, Object> stressTest(@RequestParam(defaultValue = "demo-pool") String poolName) {
        DynamicThreadPoolWrapper wrapper = registry.getThreadPool(poolName);
        if (wrapper == null) {
            return Map.of("success", false, "message", "Thread pool not found: " + poolName);
        }

        // 提交大量任务以触发告警
        int taskCount = 200;
        int submitted = 0;

        for (int i = 0; i < taskCount; i++) {
            int taskId = taskCounter.incrementAndGet();
            try {
                wrapper.execute(() -> {
                    try {
                        TimeUnit.SECONDS.sleep(10); // 长时间任务
                        log.info("Stress task-{} completed", taskId);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
                submitted++;
            } catch (Exception e) {
                log.warn("Task-{} rejected (expected for stress test)", taskId);
            }
        }

        return Map.of(
                "success", true,
                "submitted", submitted,
                "total", taskCount,
                "message", "Stress test started, check metrics and alarms"
        );
    }
}
