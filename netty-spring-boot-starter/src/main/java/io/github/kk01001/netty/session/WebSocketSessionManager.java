package io.github.archer099.netty.session;

import io.github.archer099.netty.config.NettyWebSocketProperties;
import io.github.archer099.netty.event.WebSocketMessageEvent;
import io.github.archer099.netty.event.WebSocketSessionEvent;
import io.github.archer099.netty.message.MessageDispatcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

/**
 * @author archer099
 * @date 2026-03-07 10:00:00
 * @description WebSocket 会话管理器，管理所有本机会话，支持连接数限制
 */
@Slf4j
public class WebSocketSessionManager implements MessageDispatcher {

    /**
     * sessionId -> session
     */
    protected final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    /**
     * userId -> sessionId
     */
    protected final Map<String, String> userIdSessions = new ConcurrentHashMap<>();

    private final ScheduledExecutorService scheduler;
    private final Duration sessionTimeout;
    private final int maxConnections;
    private final ApplicationEventPublisher eventPublisher;
    private final AtomicInteger connectionCount = new AtomicInteger(0);

    /**
     * 会话关闭回调，由外部设置（用于超时清理时触发 @OnClose）
     */
    private volatile SessionCloseCallback closeCallback;

    public WebSocketSessionManager(
            ScheduledExecutorService scheduler,
            NettyWebSocketProperties properties,
            ApplicationEventPublisher eventPublisher) {
        this.scheduler = scheduler;
        this.sessionTimeout = properties.getSessionTimeout();
        this.maxConnections = properties.getMaxConnections();
        this.eventPublisher = eventPublisher;
        startSessionCleanup();
    }

    /**
     * 设置会话关闭回调
     */
    public void setCloseCallback(SessionCloseCallback callback) {
        this.closeCallback = callback;
    }

    /**
     * 是否可以接受新连接
     */
    public boolean canAcceptConnection() {
        if (maxConnections <= 0) {
            return true;
        }
        return connectionCount.get() < maxConnections;
    }

    /**
     * 添加会话
     */
    public void addSession(WebSocketSession session) {
        if (session == null) {
            throw new IllegalArgumentException("session不能为空");
        }
        sessions.put(session.getId(), session);
        connectionCount.incrementAndGet();

        if (StringUtils.hasText(session.getUserId())) {
            userIdSessions.put(session.getUserId(), session.getId());
        }

        log.debug("添加会话: userId={}, sessionId={}, total={}", 
                session.getUserId(), session.getId(), connectionCount.get());
        eventPublisher.publishEvent(new WebSocketSessionEvent(this, session, WebSocketSessionEvent.EventType.ADD));
    }

    /**
     * 移除会话
     */
    public void removeSession(String sessionId) {
        WebSocketSession session = sessions.remove(sessionId);
        if (session != null) {
            connectionCount.decrementAndGet();
            if (StringUtils.hasText(session.getUserId())) {
                userIdSessions.remove(session.getUserId());
            }
            session.close();
            log.debug("移除会话: userId={}, sessionId={}, total={}",
                    session.getUserId(), sessionId, connectionCount.get());
            eventPublisher.publishEvent(
                    new WebSocketSessionEvent(this, session, WebSocketSessionEvent.EventType.REMOVE));
        }
    }

    /**
     * 获取所有会话
     */
    public Map<String, WebSocketSession> getSessions() {
        return sessions;
    }

    @Override
    public void broadcast(String message) {
        broadcast(message, session -> true);
    }

    @Override
    public void broadcastLocal(String message, Predicate<WebSocketSession> filter) {
        if (!StringUtils.hasText(message) || CollectionUtils.isEmpty(sessions)) {
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

    @Override
    public void broadcast(String message, Predicate<WebSocketSession> filter) {
        broadcastLocal(message, filter);
        log.debug("广播消息: message={}", message);
        eventPublisher.publishEvent(new WebSocketMessageEvent(this, message, null));
    }

    /**
     * 获取指定会话
     */
    public WebSocketSession getSession(String sessionId) {
        return sessions.get(sessionId);
    }

    private void startSessionCleanup() {
        scheduler.scheduleAtFixedRate(this::cleanupInactiveSessions,
                sessionTimeout.toMillis(), sessionTimeout.toMillis(), TimeUnit.MILLISECONDS);
    }

    private void cleanupInactiveSessions() {
        long now = System.currentTimeMillis();
        sessions.values().removeIf(session -> {
            if (!session.isActive() || now - session.getLastActiveTime() > sessionTimeout.toMillis()) {
                connectionCount.decrementAndGet();
                if (StringUtils.hasText(session.getUserId())) {
                    userIdSessions.remove(session.getUserId());
                }
                if (closeCallback != null) {
                    try {
                        closeCallback.onSessionClose(session);
                    } catch (Exception e) {
                        log.error("执行会话关闭回调失败: sessionId={}", session.getId(), e);
                    }
                }
                session.close();
                log.debug("清理不活跃会话: userId={}, sessionId={}", session.getUserId(), session.getId());
                eventPublisher.publishEvent(
                        new WebSocketSessionEvent(this, session, WebSocketSessionEvent.EventType.REMOVE));
                return true;
            }
            return false;
        });
    }

    @Override
    public void sendToSession(String sessionId, String message) {
        boolean local = sendToSessionLocal(sessionId, message);
        if (!local) {
            eventPublisher.publishEvent(new WebSocketMessageEvent(this, message, sessionId));
        }
    }

    @Override
    public boolean sendToSessionLocal(String sessionId, String message) {
        if (!StringUtils.hasText(message)) {
            return true;
        }
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

    @Override
    public boolean sendToUser(String userId, String message) {
        String sessionId = userIdSessions.get(userId);
        if (!StringUtils.hasText(sessionId)) {
            log.warn("发送消息失败, 未找到匹配的session: userId={}", userId);
            return false;
        }
        sendToSession(sessionId, message);
        return true;
    }

    @Override
    public int getSessionCount() {
        return connectionCount.get();
    }

    @Override
    public Set<String> getUserIds() {
        return userIdSessions.keySet();
    }

    /**
     * 会话关闭回调接口
     */
    @FunctionalInterface
    public interface SessionCloseCallback {
        void onSessionClose(WebSocketSession session);
    }
}
