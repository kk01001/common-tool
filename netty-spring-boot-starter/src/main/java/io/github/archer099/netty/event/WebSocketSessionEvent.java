package io.github.archer099.netty.event;

import io.github.archer099.netty.session.WebSocketSession;
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