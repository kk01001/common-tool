package io.github.kk01001.netty.cluster;

import io.github.kk01001.netty.session.WebSocketSession;

/**
 * WebSocket集群管理接口
 */
public interface WebSocketClusterManager {
    
    /**
     * 添加会话
     */
    void addSession(WebSocketSession session);
    
    /**
     * 移除会话
     */
    void removeSession(String sessionId);
    
    /**
     * 广播消息
     * @param message 消息
     * @param targetSessionId 目标会话ID
     */
    void broadcast(String message, String targetSessionId);
    
    /**
     * 获取节点ID
     */
    String getNodeId();
    
    /**
     * 初始化
     */
    void init();
    
    /**
     * 销毁
     */
    void destroy();
} 