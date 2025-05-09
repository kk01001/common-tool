package io.github.kk01001.netty.cluster;

/**
 * 集群消息处理器接口
 * 用于处理从其他节点接收到的消息
 */
public interface ClusterMessageHandler {

    /**
     * 处理广播消息
     * @param message 消息内容
     */
    void handleBroadcastMessage(String message);

    /**
     * 处理广播消息 只处理本机的消息
     *
     * @param message 消息内容
     */
    void handleBroadcastLocalMessage(String message);
    
    /**
     * 处理私聊消息
     * @param targetSessionId 目标会话ID
     * @param message 消息内容
     */
    void handlePrivateMessage(String targetSessionId, String message, boolean local);
    
    /**
     * 处理节点事件
     * @param nodeId 节点ID
     * @param event 事件类型
     */
    default void handleNodeEvent(String nodeId, NodeEvent event) {
        // 默认实现为空，子类可以选择性实现
    }

    /**
     * 节点事件类型
     */
    enum NodeEvent {
        /**
         * 节点上线
         */
        NODE_UP,
        
        /**
         * 节点下线
         */
        NODE_DOWN,
        
        /**
         * 节点心跳超时
         */
        NODE_TIMEOUT
    }
} 