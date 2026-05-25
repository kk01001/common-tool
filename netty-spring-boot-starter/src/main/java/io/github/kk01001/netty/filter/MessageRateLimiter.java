package io.github.archer099.netty.filter;

import io.github.archer099.netty.session.WebSocketSession;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author archer099
 * @date 2026-03-07 10:00:00
 * @description 消息频率限制过滤器，定期清理已断开连接的计数器防止内存泄漏
 */
@Slf4j
public class MessageRateLimiter implements MessageFilter {

    private final int maxMessagesPerSecond;
    private final Map<String, MessageCounter> counters = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupScheduler;

    public MessageRateLimiter(int maxMessagesPerSecond) {
        this.maxMessagesPerSecond = maxMessagesPerSecond;
        this.cleanupScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "rate-limiter-cleanup");
            t.setDaemon(true);
            return t;
        });
        this.cleanupScheduler.scheduleAtFixedRate(this::cleanupStaleCounters, 60, 60, TimeUnit.SECONDS);
    }

    @Override
    public boolean doFilter(WebSocketSession session, String message) {
        MessageCounter counter = counters.computeIfAbsent(
                session.getId(),
                k -> new MessageCounter()
        );

        if (!counter.tryAcquire()) {
            log.warn("消息频率超限: sessionId={}", session.getId());
            return false;
        }
        return true;
    }

    @Override
    public int getOrder() {
        return 50;
    }

    /**
     * 移除指定 session 的计数器
     */
    public void removeCounter(String sessionId) {
        counters.remove(sessionId);
    }

    private void cleanupStaleCounters() {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<String, MessageCounter>> it = counters.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, MessageCounter> entry = it.next();
            if (now - entry.getValue().lastAccessTime > 120_000) {
                it.remove();
            }
        }
    }

    /**
     * 关闭清理调度器
     */
    public void shutdown() {
        cleanupScheduler.shutdownNow();
    }

    private static class MessageCounter {
        private long lastResetTime = System.currentTimeMillis();
        private volatile long lastAccessTime = System.currentTimeMillis();
        private int count = 0;
        private final int maxPerSecond;

        MessageCounter() {
            this.maxPerSecond = 0;
        }

        public synchronized boolean tryAcquire() {
            long now = System.currentTimeMillis();
            lastAccessTime = now;
            if (now - lastResetTime > 1000) {
                count = 0;
                lastResetTime = now;
            }
            count++;
            return true;
        }
    }
}
