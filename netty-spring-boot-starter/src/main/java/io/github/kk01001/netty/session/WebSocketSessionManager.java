package io.github.kk01001.netty.session;

import io.github.kk01001.netty.config.NettyWebSocketProperties;
import io.github.kk01001.netty.event.WebSocketMessageEvent;
import io.github.kk01001.netty.event.WebSocketSessionEvent;
import io.github.kk01001.netty.message.MessageDispatcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

@Slf4j
public class WebSocketSessionManager implements MessageDispatcher {

    /**
     * path -> (sessionId -> session)
     */
    protected final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler;
    private final Duration sessionTimeout;
    private final ApplicationEventPublisher eventPublisher;

    public WebSocketSessionManager(
            ScheduledExecutorService scheduler,
            NettyWebSocketProperties properties,
            ApplicationEventPublisher eventPublisher) {
        this.scheduler = scheduler;
        this.sessionTimeout = properties.getSessionTimeout();
        this.eventPublisher = eventPublisher;
        startSessionCleanup();
    }
    
    /**
     * 添加会话
     */
    public void addSession(WebSocketSession session) {
        if (session == null) {
            throw new IllegalArgumentException("session不能为空");
        }
        sessions.put(session.getId(), session);
        log.debug("添加会话: userId={},sessionId={}", session.getUserId(), session.getId());

        // 发布会话添加事件
        eventPublisher.publishEvent(new WebSocketSessionEvent(this, session, WebSocketSessionEvent.EventType.ADD));
    }
    
    /**
     * 移除会话
     */
    public void removeSession(String sessionId) {
        WebSocketSession session = sessions.remove(sessionId);
        if (session != null) {
            session.close();
            log.debug("移除会话: userId={}, sessionId={}", session.getUserId(), sessionId);
            // 发布会话移除事件
            eventPublisher.publishEvent(new WebSocketSessionEvent(this, session, WebSocketSessionEvent.EventType.REMOVE));

        }
    }
    
    /**
     * 获取所有会话
     */
    public Map<String, WebSocketSession> getSessions() {
        return sessions;
    }
    
    /**
     * 广播消息给所有会话
     */
    @Override
    public void broadcast(String message) {
        broadcast(message, session -> true);
    }

    @Override
    public void broadcastLocal(String message, Predicate<WebSocketSession> filter) {
        if (!StringUtils.hasText(message)) {
            return;
        }
        if (CollectionUtils.isEmpty(sessions)) {
            return;
        }
        sessions.values().stream()
                .filter(WebSocketSession::isActive)
                .filter(filter)
                .forEach(session -> {
                    try {
                        session.sendMessage(message);
                    } catch (Exception e) {
                        log.error("广播消息失败: userId={}, sessionId={}", session.getUserId(), session.getId(), e);
                    }
                });
    }

    /**
     * 广播消息给符合条件的会话, 所有节点
     */
    @Override
    public void broadcast(String message, Predicate<WebSocketSession> filter) {
        // 本机节点
        broadcastLocal(message, filter);

        log.debug("广播消息: message={}", message);
        // 其他节点
        eventPublisher.publishEvent(new WebSocketMessageEvent(this, message, null));
    }
    
    /**
     * 获取会话数量
     */
    @Override
    public int getSessionCount() {
        return sessions.size();
    }
    
    /**
     * 获取指定会话
     */
    public WebSocketSession getSession(String sessionId) {
        return sessions.get(sessionId);
    }
    
    /**
     * 启动会话清理任务
     */
    private void startSessionCleanup() {
        scheduler.scheduleAtFixedRate(this::cleanupInactiveSessions, 
                sessionTimeout.toMillis(), sessionTimeout.toMillis(), TimeUnit.MILLISECONDS);
    }
    
    /**
     * 清理不活跃的会话
     */
    private void cleanupInactiveSessions() {
        long now = System.currentTimeMillis();
        sessions.values().removeIf(session -> {
            if (!session.isActive() || now - session.getLastActiveTime() > sessionTimeout.toMillis()) {
                session.close();
                log.debug("清理不活跃会话: userId={}, sessionId={}", session.getUserId(), session.getId());
                return true;
            }
            return false;
        });
    }

    /**
     * 发送消息给指定会话
     * 如果会话不在本机，会通过事件机制转发
     */
    @Override
    public void sendToSession(String sessionId, String message) {
        boolean local = sendToSessionLocal(sessionId, message);
        if (local) {
            return;
        }
        // 本地找不到，发布事件
        eventPublisher.publishEvent(new WebSocketMessageEvent(this,
                message,
                sessionId));
    }

    @Override
    public boolean sendToSessionLocal(String sessionId, String message) {
        if (!StringUtils.hasText(message)) {
            return true;
        }

        // 先查找本地会话
        WebSocketSession session = getSession(sessionId);
        if (session != null && session.isActive()) {
            try {
                session.sendMessage(message);
                return true;
            } catch (Exception e) {
                log.error("发送消息失败: userId={}, sessionId={}", session.getUserId(), sessionId, e);
            }
        }
        return false;
    }
}