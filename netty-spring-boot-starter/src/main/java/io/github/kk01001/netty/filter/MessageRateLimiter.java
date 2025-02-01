package io.github.kk01001.netty.filter;

import io.github.kk01001.netty.session.WebSocketSession;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class MessageRateLimiter implements MessageFilter {
    
    private final int maxMessagesPerSecond;
    private final Map<String, MessageCounter> counters = new ConcurrentHashMap<>();
    
    public MessageRateLimiter(int maxMessagesPerSecond) {
        this.maxMessagesPerSecond = maxMessagesPerSecond;
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
    
    private class MessageCounter {
        private long lastResetTime = System.currentTimeMillis();
        private int count = 0;
        
        public synchronized boolean tryAcquire() {
            long now = System.currentTimeMillis();
            if (now - lastResetTime > 1000) {
                count = 0;
                lastResetTime = now;
            }
            
            if (count >= maxMessagesPerSecond) {
                return false;
            }
            
            count++;
            return true;
        }
    }
} 