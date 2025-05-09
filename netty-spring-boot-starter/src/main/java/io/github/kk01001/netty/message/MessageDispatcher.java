package io.github.kk01001.netty.message;

import io.github.kk01001.netty.session.WebSocketSession;

import java.util.function.Predicate;

/**
 * 消息分发器
 */
public interface MessageDispatcher {
    
    /**
     * 广播消息
     */
    void broadcast(String message);

    /**
     * 广播消息 只发送本机
     *
     * @param message 消息
     */
    void broadcastLocal(String message, Predicate<WebSocketSession> filter);

    /**
     * 过滤器广播消息
     */
    void broadcast(String message, Predicate<WebSocketSession> filter);
    
    /**
     * 发送消息给指定会话
     */
    void sendToSession(String sessionId, String message);

    /**
     * 发送消息给指定会话, 只发送本机
     */
    boolean sendToSessionLocal(String sessionId, String message);

    /**
     * 获取会话数量
     */
    int getSessionCount();

}