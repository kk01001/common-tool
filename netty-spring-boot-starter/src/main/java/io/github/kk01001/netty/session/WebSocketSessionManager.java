package io.github.kk01001.netty.session;

import io.github.kk01001.netty.config.NettyWebSocketProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

@Slf4j
public class WebSocketSessionManager {
    
    protected final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler;
    private final Duration sessionTimeout;
    
    public WebSocketSessionManager(ScheduledExecutorService scheduler, NettyWebSocketProperties properties) {
        this.scheduler = scheduler;
        this.sessionTimeout = properties.getSessionTimeout();
        startSessionCleanup();
    }
    
    /**
     * 添加会话
     */
    public void addSession(String path, WebSocketSession session) {
        if (!StringUtils.hasText(path) || session == null) {
            throw new IllegalArgumentException("path和session不能为空");
        }
        sessions.put(session.getId(), session);
        log.debug("添加会话: path={}, sessionId={}", path, session.getId());
    }
    
    /**
     * 移除会话
     */
    public void removeSession(String path, String sessionId) {
        WebSocketSession removed = sessions.remove(sessionId);
        if (removed != null) {
            removed.close();
            log.debug("移除会话: path={}, sessionId={}", path, sessionId);
        }
    }
    
    /**
     * 获取所有会话
     */
    public Set<WebSocketSession> getSessions(String path) {
        return Set.copyOf(sessions.values());
    }
    
    /**
     * 广播消息给所有会话
     */
    public void broadcast(String path, String message) {
        broadcast(path, message, session -> true);
    }
    
    /**
     * 广播消息给符合条件的会话
     */
    public void broadcast(String path, String message, Predicate<WebSocketSession> filter) {
        if (!StringUtils.hasText(message)) {
            return;
        }
        sessions.values().stream()
                .filter(session -> session.getPath().equals(path))
                .filter(WebSocketSession::isActive)
                .filter(filter)
                .forEach(session -> {
                    try {
                        session.sendMessage(message);
                    } catch (Exception e) {
                        log.error("广播消息失败: sessionId={}", session.getId(), e);
                        removeSession(path, session.getId());
                    }
                });
    }
    
    /**
     * 获取会话数量
     */
    public int getSessionCount(String path) {
        return (int) sessions.values().stream()
                .filter(session -> session.getPath().equals(path))
                .count();
    }
    
    /**
     * 获取指定会话
     */
    public WebSocketSession getSession(String path, String sessionId) {
        WebSocketSession session = sessions.get(sessionId);
        return session != null && session.getPath().equals(path) ? session : null;
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
                log.debug("清理不活跃会话: path={}, sessionId={}", session.getPath(), session.getId());
                return true;
            }
            return false;
        });
    }
} 