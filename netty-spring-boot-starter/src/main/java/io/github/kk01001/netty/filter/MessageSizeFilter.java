package io.github.kk01001.netty.filter;

import io.github.kk01001.netty.session.WebSocketSession;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MessageSizeFilter implements MessageFilter {
    
    private final int maxMessageSize;
    
    public MessageSizeFilter(int maxMessageSize) {
        this.maxMessageSize = maxMessageSize;
    }
    
    @Override
    public boolean doFilter(WebSocketSession session, String message) {
        if (message.length() > maxMessageSize) {
            log.warn("消息超过最大长度: sessionId={}, length={}, maxSize={}", 
                    session.getId(), message.length(), maxMessageSize);
            return false;
        }
        return true;
    }
    
    @Override
    public int getOrder() {
        return 0; // 优先处理大小限制
    }
} 