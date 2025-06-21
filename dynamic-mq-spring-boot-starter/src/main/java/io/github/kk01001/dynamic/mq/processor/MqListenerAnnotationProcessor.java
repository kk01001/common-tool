package io.github.kk01001.dynamic.mq.processor;

import io.github.kk01001.dynamic.mq.annotation.DynamicMqListener;
import io.github.kk01001.dynamic.mq.consumer.ConsumerRegistration;
import io.github.kk01001.dynamic.mq.consumer.ConsumerRegistry;
import io.github.kk01001.dynamic.mq.consumer.DynamicMqConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @author linshiqiang
 * @date 2025-06-21 20:48:56
 * @description MQ监听器注解处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MqListenerAnnotationProcessor implements BeanPostProcessor {
    
    private final ConsumerRegistry consumerRegistry;
    
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        
        // 检查是否有@DynamicMqListener注解
        DynamicMqListener annotation = AnnotationUtils.findAnnotation(beanClass, DynamicMqListener.class);
        if (annotation == null) {
            return bean;
        }
        
        // 检查是否实现了DynamicMqConsumer接口
        if (!(bean instanceof DynamicMqConsumer)) {
            log.warn("类 {} 标注了@DynamicMqListener注解但未实现DynamicMqConsumer接口，跳过注册", beanClass.getName());
            return bean;
        }
        
        // 注册消费者
        registerConsumer((DynamicMqConsumer) bean, annotation, beanName);
        
        return bean;
    }
    
    /**
     * 注册消费者
     */
    private void registerConsumer(DynamicMqConsumer consumer, DynamicMqListener annotation, String beanName) {
        try {
            // 构建注册信息
            ConsumerRegistration registration = buildRegistration(consumer, annotation, beanName);
            
            // 注册到注册中心
            String consumerId = consumerRegistry.register(registration);
            
            log.info("通过注解注册消费者成功: beanName={}, consumerId={}, topic={}, tag={}", 
                    beanName, consumerId, registration.getTopic(), registration.getTag());
            
        } catch (Exception e) {
            log.error("注册消费者失败: beanName={}, error={}", beanName, e.getMessage(), e);
            throw new RuntimeException("注册消费者失败: " + beanName, e);
        }
    }
    
    /**
     * 构建注册信息
     */
    private ConsumerRegistration buildRegistration(DynamicMqConsumer consumer, DynamicMqListener annotation, String beanName) {
        ConsumerRegistration.ConsumerRegistrationBuilder builder = ConsumerRegistration.builder()
                .consumer(consumer)
                .consumerId(beanName); // 使用Bean名称作为消费者ID
        
        // 设置MQ类型
        if (StringUtils.hasText(annotation.type())) {
            builder.mqType(annotation.type());
        }
        
        // 设置消费者组
        if (StringUtils.hasText(annotation.group())) {
            builder.group(annotation.group());
        }
        
        // 设置主题
        if (StringUtils.hasText(annotation.topic())) {
            builder.topic(annotation.topic());
        } else {
            throw new IllegalArgumentException("@DynamicMqListener注解必须指定topic属性");
        }
        
        // 设置标签
        if (StringUtils.hasText(annotation.tag())) {
            builder.tag(annotation.tag());
        }
        
        // 设置批量拉取大小
        if (annotation.pullBatchSize() > 0) {
            builder.pullBatchSize(annotation.pullBatchSize());
        }
        
        // 设置批量消费大小
        if (annotation.consumeMessageBatchMaxSize() > 0) {
            builder.consumeMessageBatchMaxSize(annotation.consumeMessageBatchMaxSize());
        }
        
        // 设置消费线程数
        if (annotation.consumeThreadMin() > 0) {
            builder.consumeThreadMin(annotation.consumeThreadMin());
        }
        if (annotation.consumeThreadMax() > 0) {
            builder.consumeThreadMax(annotation.consumeThreadMax());
        }
        
        // 设置消费超时
        if (annotation.consumeTimeout() > 0) {
            builder.consumeTimeout(annotation.consumeTimeout());
        }
        
        // 设置消费模式
        builder.consumeMode(annotation.consumeMode());
        
        // 设置消息模型
        builder.messageModel(annotation.messageModel());
        
        // 设置消息选择器
        builder.selectorType(annotation.selectorType());
        if (StringUtils.hasText(annotation.selectorExpression())) {
            builder.selectorExpression(annotation.selectorExpression());
        }
        
        // 设置其他属性
        builder.enableMsgTrace(annotation.enableMsgTrace());
        builder.autoStartup(annotation.autoStartup());
        
        return builder.build();
    }
}
