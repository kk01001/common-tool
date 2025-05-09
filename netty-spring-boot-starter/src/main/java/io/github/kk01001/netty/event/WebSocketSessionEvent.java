package io.github.kk01001.netty.event;

import io.github.kk01001.netty.session.WebSocketSession;
import org.springframework.context.ApplicationEvent;

public class WebSocketSessionEvent extends ApplicationEvent {
    private final WebSocketSession session;
    private final EventType eventType;

    public WebSocketSessionEvent(Object source, WebSocketSession session, EventType eventType) {
        super(source);
        this.session = session;
        this.eventType = eventType;
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