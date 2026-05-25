package io.github.archer099.resilience4j.example.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 模拟远程服务
 *
 * @author archer099
 */
@Service
public class RemoteService {

    private static final Logger log = LoggerFactory.getLogger(RemoteService.class);
    private final Random random = new Random();
    private final AtomicInteger callCount = new AtomicInteger(0);

    /**
     * 模拟不稳定的服务调用
     */
    public String unstableCall(int failureRate) {
        int count = callCount.incrementAndGet();
        log.info("Remote call #{}", count);
        
        if (random.nextInt(100) < failureRate) {
            throw new RuntimeException("Remote service error (simulated)");
        }
        
        return "Success response #" + count;
    }

    /**
     * 模拟慢调用
     */
    public String slowCall(long delayMs) {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "Slow response after " + delayMs + "ms";
    }

    /**
     * 模拟正常调用
     */
    public String normalCall() {
        return "Normal response #" + callCount.incrementAndGet();
    }

    /**
     * 重置计数器
     */
    public void resetCounter() {
        callCount.set(0);
    }

    /**
     * 获取调用次数
     */
    public int getCallCount() {
        return callCount.get();
    }
}
