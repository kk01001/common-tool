package io.github.kk01001.netty.event;

import io.github.kk01001.netty.session.WebSocketSession;
import org.springframework.context.ApplicationEvent;

public class WebSocketSessionEvent extends ApplicationEvent {
    private final String path;
    private final WebSocketSession session;
    private final EventType eventType;

    public WebSocketSessionEvent(Object source, String path, WebSocketSession session, EventType eventType) {
        super(source);
        this.path = path;
        this.session = session;
        this.eventType = eventType;
    }

    public String getPath() {
        return path;
    }

    public WebSocketSession getSession() {
        return session;
    }

    public EventType getEventType() {
        return eventType;
    }

    public enum EventType {
        ADD, REMOVE
    }
} 