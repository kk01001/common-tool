package io.github.kk01001.dynamic.mq.consumer;

import io.github.kk01001.dynamic.mq.enums.ConsumeMode;
import io.github.kk01001.dynamic.mq.enums.MessageModel;
import io.github.kk01001.dynamic.mq.enums.MessageSelectorType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author linshiqiang
 * @date 2025-06-21 20:48:56
 * @description 消费者注册信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsumerRegistration {
    
    /**
     * 消费者实例
     */
    private DynamicMqConsumer consumer;
    
    /**
     * 消费者ID（唯一标识）
     */
    private String consumerId;
    
    /**
     * MQ类型
     */
    private String mqType;
    
    /**
     * 消费者组名
     */
    private String group;
    
    /**
     * 主题
     */
    private String topic;
    
    /**
     * 标签
     */
    private String tag;
    
    /**
     * 批量拉取大小
     */
    private Integer pullBatchSize;
    
    /**
     * 批量消费大小
     */
    private Integer consumeMessageBatchMaxSize;
    
    /**
     * 最小消费线程数
     */
    private Integer consumeThreadMin;
    
    /**
     * 最大消费线程数
     */
    private Integer consumeThreadMax;
    
    /**
     * 消费超时时间
     */
    private Long consumeTimeout;
    
    /**
     * 消费模式
     */
    private ConsumeMode consumeMode;
    
    /**
     * 消息模型
     */
    private MessageModel messageModel;
    
    /**
     * 消息选择器类型
     */
    private MessageSelectorType selectorType;
    
    /**
     * 消息选择器表达式
     */
    private String selectorExpression;
    
    /**
     * 是否开启消息轨迹
     */
    private Boolean enableMsgTrace;
    
    /**
     * 是否自动启动
     */
    private Boolean autoStartup;
    
    /**
     * 是否已启动
     */
    private Boolean started;
    
    /**
     * 创建时间
     */
    private Long createTime;
}
