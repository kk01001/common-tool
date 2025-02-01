package io.github.kk01001.netty.message;

/**
 * 消息分发器
 */
public interface MessageDispatcher {
    
    /**
     * 广播消息
     */
    void broadcast(String path, String message);
    
    /**
     * 发送消息给指定会话
     */
    void sendToSession(String path, String sessionId, String message);
} 