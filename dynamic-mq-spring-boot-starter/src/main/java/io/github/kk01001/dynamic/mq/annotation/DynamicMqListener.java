package io.github.kk01001.dynamic.mq.annotation;

import io.github.kk01001.dynamic.mq.enums.ConsumeMode;
import io.github.kk01001.dynamic.mq.enums.MessageModel;
import io.github.kk01001.dynamic.mq.enums.MessageSelectorType;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @author linshiqiang
 * @date 2025-06-21 20:48:56
 * @description 动态MQ监听器注解
 */
@Component
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface DynamicMqListener {
    
    /**
     * 消息队列类型，为空时使用默认配置
     *
     * @return 消息队列类型
     */
    String type() default "";
    
    /**
     * 消费者组名，为空时使用默认配置
     *
     * @return 消费者组名
     */
    String group() default "";
    
    /**
     * 消息主题
     *
     * @return 消息主题
     */
    String topic() default "";
    
    /**
     * 消息标签/路由键
     *
     * @return 消息标签
     */
    String tag() default "";
    
    /**
     * 从Broker端批量拉取消息大小
     *
     * @return 默认拉取32条消息
     */
    int pullBatchSize() default 0;
    
    /**
     * 批量消费消息数量
     *
     * @return 默认消费1条消息
     */
    int consumeMessageBatchMaxSize() default 0;
    
    /**
     * 最小消费线程数
     *
     * @return 默认1个线程
     */
    int consumeThreadMin() default 0;
    
    /**
     * 最大消费线程数
     *
     * @return 默认10个线程
     */
    int consumeThreadMax() default 0;
    
    /**
     * 消费超时时间（毫秒）
     *
     * @return 默认15分钟
     */
    long consumeTimeout() default 0;
    
    /**
     * 消费模式
     *
     * @return 默认并发消费
     */
    ConsumeMode consumeMode() default ConsumeMode.UNSET;
    
    /**
     * 消息模型
     *
     * @return 默认集群模式
     */
    MessageModel messageModel() default MessageModel.UNSET;
    
    /**
     * 消息过滤类型
     *
     * @return 默认按Tag过滤
     */
    MessageSelectorType selectorType() default MessageSelectorType.UNSET;
    
    /**
     * 消息过滤表达式
     *
     * @return 默认全匹配
     */
    String selectorExpression() default "*";
    
    /**
     * 是否开启消息轨迹追踪
     *
     * @return 默认开启
     */
    boolean enableMsgTrace() default true;
    
    /**
     * 是否自动启动消费者
     *
     * @return 默认自动启动
     */
    boolean autoStartup() default true;
}
