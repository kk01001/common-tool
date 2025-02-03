package io.github.kk01001.netty.cluster.model;

import lombok.Data;

@Data
public class BroadcastMessage {

    /**
     * 路径
     */
    private final String path;

    /**
     * 消息
     */
    private final String message;

    /**
     * 节点ID
     */
    private final String nodeId;

    /**
     * 目标会话ID
     */
    private final String targetSessionId;

    /**
     * 消息发送时间
     */
    private final long timestamp = System.currentTimeMillis();

    public BroadcastMessage(String path,
                            String message,
                            String nodeId,
                            String targetSessionId
    ) {
        this.path = path;
        this.message = message;
        this.nodeId = nodeId;
        this.targetSessionId = targetSessionId;
    }
} 