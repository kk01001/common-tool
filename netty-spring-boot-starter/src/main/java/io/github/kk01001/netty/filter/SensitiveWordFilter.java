package io.github.kk01001.netty.filter;

import io.github.kk01001.netty.session.WebSocketSession;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@Slf4j
public class SensitiveWordFilter implements MessageFilter {
    
    private final Set<String> sensitiveWords;
    
    public SensitiveWordFilter(Set<String> sensitiveWords) {
        this.sensitiveWords = sensitiveWords;
    }
    
    @Override
    public boolean doFilter(WebSocketSession session, String message) {
        for (String word : sensitiveWords) {
            if (message.contains(word)) {
                log.warn("消息包含敏感词: sessionId={}, word={}", session.getId(), word);
                return false;
            }
        }
        return true;
    }
    
    @Override
    public int getOrder() {
        return 100;
    }
} 