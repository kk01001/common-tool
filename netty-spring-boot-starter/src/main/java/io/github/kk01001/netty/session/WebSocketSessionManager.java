package io.github.kk01001.netty.session;

import io.github.kk01001.netty.cluster.WebSocketClusterManager;
import io.github.kk01001.netty.config.NettyWebSocketProperties;
import io.github.kk01001.netty.event.WebSocketMessageEvent;
import io.github.kk01001.netty.message.MessageDispatcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

@Slf4j
public class WebSocketSessionManager implements MessageDispatcher {

    /**
     * path -> (sessionId -> session)
     */
    protected final Map<String, Map<String, WebSocketSession>> sessions = new ConcurrentHashMap<>();
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
    public void addSession(String path, WebSocketSession session) {
        if (!StringUtils.hasText(path) || session == null) {
            throw new IllegalArgumentException("path和session不能为空");
        }
        sessions.computeIfAbsent(path, k -> new ConcurrentHashMap<>())
                .put(session.getId(), session);
        log.debug("添加会话: path={}, sessionId={}", path, session.getId());
    }
    
    /**
     * 移除会话
     */
    public void removeSession(String path, String sessionId) {
        Map<String, WebSocketSession> pathSessions = sessions.get(path);
        if (pathSessions != null) {
            WebSocketSession removed = pathSessions.remove(sessionId);
            if (removed != null) {
                removed.close();
                log.debug("移除会话: path={}, sessionId={}", path, sessionId);
            }
            // 如果该路径下没有会话了，移除该路径
            if (pathSessions.isEmpty()) {
                sessions.remove(path);
            }
        }
    }
    
    /**
     * 获取所有会话
     */
    public Set<WebSocketSession> getSessions(String path) {
        Map<String, WebSocketSession> pathSessions = sessions.get(path);
        return pathSessions != null ? Set.copyOf(pathSessions.values()) : Set.of();
    }
    
    /**
     * 广播消息给所有会话
     */
    @Override
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
        Map<String, WebSocketSession> pathSessions = sessions.get(path);
        if (pathSessions != null) {
            pathSessions.values().stream()
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
    }
    
    /**
     * 获取会话数量
     */
    public int getSessionCount(String path) {
        Map<String, WebSocketSession> pathSessions = sessions.get(path);
        return pathSessions != null ? pathSessions.size() : 0;
    }
    
    /**
     * 获取指定会话
     */
    public WebSocketSession getSession(String path, String sessionId) {
        Map<String, WebSocketSession> pathSessions = sessions.get(path);
        return pathSessions != null ? pathSessions.get(sessionId) : null;
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
        sessions.forEach((path, pathSessions) ->
                pathSessions.values().removeIf(session -> {
                    if (!session.isActive() || now - session.getLastActiveTime() > sessionTimeout.toMillis()) {
                        session.close();
                        log.debug("清理不活跃会话: path={}, sessionId={}", path, session.getId());
                        return true;
                    }
                    return false;
                })
        );
        // 清理空路径
        sessions.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    /**
     * 发送消息给指定会话
     * 如果会话不在本机，会通过事件机制转发
     */
    @Override
    public void sendToSession(String path, String sessionId, String message) {
        if (!StringUtils.hasText(message)) {
            return;
        }
        
        // 先查找本地会话
        WebSocketSession session = getSession(path, sessionId);
        if (session != null && session.isActive()) {
            try {
                session.sendMessage(message);
                return;
            } catch (Exception e) {
                log.error("发送消息失败: sessionId={}", sessionId, e);
                removeSession(path, sessionId);
                return;
            }
        }
        
        // 本地找不到，发布事件
        eventPublisher.publishEvent(new WebSocketMessageEvent(this, path, message, sessionId));
    }
}