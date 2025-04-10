package io.github.kk01001.netty.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class WebSocketMessageEvent extends ApplicationEvent {

    /**
     * 消息路径
     */
    private final String path;

    /**
     * 消息内容
     */
    private final String message;

    /**
     * 消息目标会话ID
     * 为null表示广播消息 发给所有会话
     */
    private final String targetSessionId;

    public WebSocketMessageEvent(Object source,
                                 String path,
                                 String message,
                                 String targetSessionId) {
        super(source);
        this.path = path;
        this.message = message;
        this.targetSessionId = targetSessionId;
    }
} 