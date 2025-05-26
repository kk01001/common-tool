package io.github.kk01001.sse.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description SSE消息模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SseMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = -8822441457554427866L;
    
    /**
     * 消息ID
     */
    private String id;

    /**
     * 消息内容
     */
    private String data;

    /**
     * 事件名称
     */
    private String event;

    /**
     * 重试时间（毫秒）
     */
    private Long retry;

    /**
     * 客户端ID
     */
    private String clientId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 主题
     */
    private String topic;

    /**
     * 是否为广播消息
     */
    private Boolean broadcast;

    /**
     * 是否为心跳消息
     */
    private Boolean heartbeat;
}
