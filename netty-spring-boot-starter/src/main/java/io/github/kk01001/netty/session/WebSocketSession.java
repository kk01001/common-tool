package io.github.kk01001.netty.session;

import io.github.kk01001.netty.trace.MessageTracer;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author kk01001
 * @date 2026-03-07 10:00:00
 * @description WebSocket 会话，封装连接通道和会话属性
 */
@Getter
@Setter
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
     * WebSocket 路径
     */
    private final String path;

    /**
     * 远程地址
     */
    private final String remoteAddress;

    /**
     * 会话管理器
     */
    private final WebSocketSessionManager sessionManager;

    /**
     * 消息追踪器
     */
    private final MessageTracer messageTracer;

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
    private String userId;

    public WebSocketSession(String id,
                            Channel channel,
                            String path,
                            String remoteAddress,
                            WebSocketSessionManager sessionManager,
                            MessageTracer messageTracer) {
        this.id = id;
        this.channel = channel;
        this.path = path;
        this.remoteAddress = remoteAddress;
        this.sessionManager = sessionManager;
        this.messageTracer = messageTracer;
    }

    /**
     * 发送文本消息
     */
    public void sendMessage(String message) {
        if (isActive()) {
            messageTracer.traceSend(this, message);
            channel.writeAndFlush(new TextWebSocketFrame(message));
            updateLastActiveTime();
        } else {
            log.warn("Channel已关闭，无法发送消息: sessionId={}", id);
        }
    }

    /**
     * 发送二进制消息
     */
    public void sendBinary(byte[] data) {
        if (isActive()) {
            channel.writeAndFlush(new BinaryWebSocketFrame(
                    channel.alloc().buffer(data.length).writeBytes(data)));
            updateLastActiveTime();
        } else {
            log.warn("Channel已关闭，无法发送二进制消息: sessionId={}", id);
        }
    }

    /**
     * 广播消息给其他会话（不包括自己）
     */
    public void broadcast(String message) {
        messageTracer.traceSend(this, message);
        sessionManager.broadcast(message, session -> !session.getId().equals(this.id));
        updateLastActiveTime();
    }

    /**
     * 广播消息给所有会话（包括自己）
     */
    public void broadcastAll(String message) {
        messageTracer.traceSend(this, message);
        sessionManager.broadcast(message);
        updateLastActiveTime();
    }

    /**
     * 给指定会话发送消息
     */
    public void sendToSession(String sessionId, String message) {
        messageTracer.traceSend(this, message);
        sessionManager.sendToSession(sessionId, message);
        updateLastActiveTime();
    }

    /**
     * 关闭会话
     */
    public void close() {
        try {
            if (channel != null && channel.isActive()) {
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

    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key) {
        return (T) attributes.get(key);
    }

    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    public void removeAttribute(String key) {
        attributes.remove(key);
    }

    /**
     * 发送ping消息
     */
    public void sendPing() {
        if (isActive()) {
            channel.writeAndFlush(new PingWebSocketFrame());
            updateLastActiveTime();
        }
    }

    /**
     * 发送pong响应
     */
    public void sendPong() {
        if (isActive()) {
            channel.writeAndFlush(new PongWebSocketFrame());
            updateLastActiveTime();
        }
    }

    /**
     * 发送pong文本响应
     */
    public void sendPongText() {
        if (isActive()) {
            channel.writeAndFlush(new TextWebSocketFrame("pong"));
            updateLastActiveTime();
        }
    }
}
