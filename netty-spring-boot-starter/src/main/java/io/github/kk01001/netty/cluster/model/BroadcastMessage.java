package io.github.kk01001.netty.cluster.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BroadcastMessage {

    /**
     * 消息
     */
    private String message;

    /**
     * 节点ID
     */
    private String nodeId;

    /**
     * 目标会话ID
     */
    private String targetSessionId;

    /**
     * 消息发送时间
     */
    private long timestamp;


    public BroadcastMessage(String message,
                            String nodeId,
                            String targetSessionId) {
        this.message = message;
        this.nodeId = nodeId;
        this.targetSessionId = targetSessionId;
        this.timestamp = System.currentTimeMillis();
    }
} 