package io.github.kk01001.dynamic.mq.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * @author linshiqiang
 * @date 2025-06-21 20:48:56
 * @description 统一消息模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MqMessage implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 消息ID
     */
    private String messageId;
    
    /**
     * 主题/队列名称
     */
    private String topic;
    
    /**
     * 标签/路由键
     */
    private String tag;
    
    /**
     * 消息体
     */
    private Object payload;
    
    /**
     * 消息头信息
     */
    private Map<String, Object> headers;
    
    /**
     * 延迟时间（毫秒）
     */
    private Long delayTime;
    
    /**
     * 消息优先级
     */
    private Integer priority;
    
    /**
     * 消息创建时间
     */
    private Long createTime;
    
    /**
     * 重试次数
     */
    private Integer retryCount;
    
    /**
     * 最大重试次数
     */
    private Integer maxRetryCount;

    /**
     * 消息属性
     */
    private Map<Object, Object> properties;

    /**
     * 获取消息属性
     */
    public Map<Object, Object> getProperties() {
        return properties;
    }

    /**
     * 设置消息属性
     */
    public void setProperties(Map<Object, Object> properties) {
        this.properties = properties;
    }
}
