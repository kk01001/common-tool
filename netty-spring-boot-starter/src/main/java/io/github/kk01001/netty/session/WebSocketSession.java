package io.github.kk01001.netty.session;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@Slf4j
public class WebSocketSession {
    /**
     * 会话ID
     */
    private final String id;
    
    /**
     * Netty通道
     */
    private final Channel channel;
    
    /**
     * WebSocket路径
     */
    private final String path;
    
    /**
     * 请求URI
     */
    private final String uri;
    
    /**
     * 会话管理器
     */
    private final WebSocketSessionManager sessionManager;
    
    /**
     * 会话属性
     */
    private final Map<String, Object> attributes = new ConcurrentHashMap<>();
    
    /**
     * 创建时间
     */
    private final long createTime = System.currentTimeMillis();
    
    /**
     * 最后活跃时间
     */
    private volatile long lastActiveTime = System.currentTimeMillis();
    
    /**
     * 用户ID
     */
    private final String userId;
    
    public WebSocketSession(String id, Channel channel, String path, String uri, WebSocketSessionManager sessionManager, String userId) {
        this.id = id;
        this.channel = channel;
        this.path = path;
        this.uri = uri;
        this.sessionManager = sessionManager;
        this.userId = userId;
    }
    
    /**
     * 发送消息
     */
    public void sendMessage(String message) {
        try {
            if (channel.isActive()) {
                channel.writeAndFlush(new TextWebSocketFrame(message));
                updateLastActiveTime();
            } else {
                log.warn("Channel已关闭，无法发送消息: sessionId={}", id);
            }
        } catch (Exception e) {
            log.error("发送消息失败: sessionId={}", id, e);
            throw new RuntimeException("发送消息失败", e);
        }
    }

    /**
     * 广播消息给同路径的其他会话（不包括自己）
     */
    public void broadcast(String message) {
        sessionManager.broadcast(path, message, session -> !session.getId().equals(this.id));
    }

    /**
     * 广播消息给所有会话（包括自己）
     */
    public void broadcastAll(String message) {
        sessionManager.broadcast(path, message);
    }

    /**
     * 给指定会话发送消息
     */
    public void sendToSession(String sessionId, String message) {
        sessionManager.sendToSession(path, sessionId, message);
    }

    /**
     * 关闭会话
     */
    public void close() {
        try {
            if (channel.isActive()) {
                channel.close();
            }
        } catch (Exception e) {
            log.error("关闭会话失败: sessionId={}", id, e);
        }
    }
    
    /**
     * 是否活跃
     */
    public boolean isActive() {
        return channel != null && channel.isActive();
    }
    
    /**
     * 更新最后活跃时间
     */
    public void updateLastActiveTime() {
        this.lastActiveTime = System.currentTimeMillis();
    }
    
    /**
     * 获取会话属性
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key) {
        return (T) attributes.get(key);
    }
    
    /**
     * 设置会话属性
     */
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }
    
    /**
     * 移除会话属性
     */
    public void removeAttribute(String key) {
        attributes.remove(key);
    }

}