package io.github.kk01001.dynamic.mq.manager;

import io.github.kk01001.dynamic.mq.config.DynamicMqProperties;
import io.github.kk01001.dynamic.mq.core.AbstractMqProducer;
import io.github.kk01001.dynamic.mq.core.MqProducer;
import io.github.kk01001.dynamic.mq.core.MqSendCallback;
import io.github.kk01001.dynamic.mq.enums.MqType;
import io.github.kk01001.dynamic.mq.factory.MqProducerFactory;
import io.github.kk01001.dynamic.mq.model.MqMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * @author linshiqiang
 * @date 2025-06-21 20:48:56
 * @description 动态MQ生产者管理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicMqProducerManager implements MqProducer {
    
    private final DynamicMqProperties properties;
    private final MqProducerFactory producerFactory;
    
    private AbstractMqProducer currentProducer;
    
    @PostConstruct
    public void init() {
        if (properties.isEnabled()) {
            switchMqType(properties.getType());
            log.info("动态MQ生产者管理器初始化完成，当前MQ类型: {}", properties.getType());
        } else {
            log.info("动态MQ功能已禁用");
        }
    }
    
    /**
     * 切换MQ类型
     *
     * @param mqType 目标MQ类型
     */
    public void switchMqType(MqType mqType) {
        if (!properties.isEnabled()) {
            throw new IllegalStateException("动态MQ功能未启用");
        }
        
        log.info("切换生产者MQ类型: {} -> {}", 
                currentProducer != null ? currentProducer.getProducerType() : "none", 
                mqType);
        
        // 获取新的生产者
        currentProducer = producerFactory.getProducer(mqType);
        
        log.info("生产者MQ类型切换完成: {}", mqType);
    }
    
    /**
     * 获取当前MQ类型
     */
    public MqType getCurrentMqType() {
        if (currentProducer == null) {
            return null;
        }
        return MqType.fromCode(currentProducer.getProducerType());
    }
    
    /**
     * 检查生产者是否可用
     */
    public boolean isAvailable() {
        return properties.isEnabled() && currentProducer != null;
    }
    
    // ==================== MqProducer 接口实现 ====================
    
    @Override
    public boolean send(MqMessage message) {
        checkAvailable();
        return currentProducer.send(message);
    }
    
    @Override
    public boolean sendDelay(MqMessage message, long delayTime) {
        checkAvailable();
        return currentProducer.sendDelay(message, delayTime);
    }

    @Override
    public void sendAsync(MqMessage message, MqSendCallback callback) {
        checkAvailable();
        currentProducer.sendAsync(message, callback);
    }

    @Override
    public void sendOneWay(MqMessage message) {
        checkAvailable();
        currentProducer.sendOneWay(message);
    }

    @Override
    public boolean sendOrderly(MqMessage message, String orderKey) {
        checkAvailable();
        return currentProducer.sendOrderly(message, orderKey);
    }

    @Override
    public boolean sendTransaction(MqMessage message, String transactionId, io.github.kk01001.dynamic.mq.core.MqTransactionListener transactionListener) {
        checkAvailable();
        return currentProducer.sendTransaction(message, transactionId, transactionListener);
    }
    
    @Override
    public boolean sendBatch(MqMessage... messages) {
        checkAvailable();
        return currentProducer.sendBatch(messages);
    }
    
    @Override
    public String getProducerType() {
        return currentProducer != null ? currentProducer.getProducerType() : "none";
    }
    
    // ==================== 便捷方法 ====================
    
    /**
     * 发送简单消息
     *
     * @param topic 主题
     * @param payload 消息体
     * @return 发送结果
     */
    public boolean sendSimple(String topic, Object payload) {
        MqMessage message = MqMessage.builder()
                .topic(topic)
                .payload(payload)
                .build();
        return send(message);
    }
    
    /**
     * 发送带标签的消息
     *
     * @param topic 主题
     * @param tag 标签
     * @param payload 消息体
     * @return 发送结果
     */
    public boolean sendWithTag(String topic, String tag, Object payload) {
        MqMessage message = MqMessage.builder()
                .topic(topic)
                .tag(tag)
                .payload(payload)
                .build();
        return send(message);
    }
    
    /**
     * 检查可用性
     */
    private void checkAvailable() {
        if (!isAvailable()) {
            throw new IllegalStateException("动态MQ生产者不可用，请检查配置和初始化状态");
        }
    }
}
