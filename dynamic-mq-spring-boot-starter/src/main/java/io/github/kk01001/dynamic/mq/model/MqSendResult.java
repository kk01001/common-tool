package io.github.kk01001.dynamic.mq.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author linshiqiang
 * @date 2025-06-21 21:41:29
 * @description MQ发送结果，通用的发送结果封装
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MqSendResult {
    
    /**
     * 消息ID
     */
    private String messageId;
    
    /**
     * 主题
     */
    private String topic;
    
    /**
     * 标签
     */
    private String tag;
    
    /**
     * 发送状态
     */
    private String status;
    
    /**
     * 队列ID（如果支持）
     */
    private String queueId;
    
    /**
     * 队列偏移量（如果支持）
     */
    private Long queueOffset;
    
    /**
     * 发送时间戳
     */
    private Long sendTimestamp;
    
    /**
     * 扩展属性
     */
    private java.util.Map<String, Object> properties;
    
    /**
     * 创建成功结果
     */
    public static MqSendResult success(String messageId, String topic) {
        return MqSendResult.builder()
                .messageId(messageId)
                .topic(topic)
                .status("SUCCESS")
                .sendTimestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * 创建失败结果
     */
    public static MqSendResult failure(String topic, String errorMessage) {
        return MqSendResult.builder()
                .topic(topic)
                .status("FAILURE")
                .sendTimestamp(System.currentTimeMillis())
                .properties(java.util.Map.of("error", errorMessage))
                .build();
    }
}
