package io.github.kk01001.netty.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class WebSocketMessageEvent extends ApplicationEvent {
    private final String path;
    private final String message;
    private final String targetSessionId; // 可能为null，表示广播消息
    
    public WebSocketMessageEvent(Object source, String path, String message, String targetSessionId) {
        super(source);
        this.path = path;
        this.message = message;
        this.targetSessionId = targetSessionId;
    }
} 