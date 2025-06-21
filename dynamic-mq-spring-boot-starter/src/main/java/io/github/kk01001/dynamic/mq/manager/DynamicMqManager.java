package io.github.kk01001.dynamic.mq.manager;

import io.github.kk01001.dynamic.mq.config.DynamicMqProperties;
import io.github.kk01001.dynamic.mq.consumer.ConsumerRegistration;
import io.github.kk01001.dynamic.mq.consumer.DynamicMqConsumer;
import io.github.kk01001.dynamic.mq.core.MqConsumer;
import io.github.kk01001.dynamic.mq.core.MqProducer;
import io.github.kk01001.dynamic.mq.core.MqSendCallback;
import io.github.kk01001.dynamic.mq.enums.MqType;
import io.github.kk01001.dynamic.mq.model.MqMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.List;

/**
 * @author linshiqiang
 * @date 2025-06-21 20:48:56
 * @description 动态MQ管理器，提供统一的MQ操作接口（门面模式）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicMqManager implements MqProducer, MqConsumer {

    private final DynamicMqProperties properties;
    private final DynamicMqProducerManager producerManager;
    private final DynamicMqConsumerManager consumerManager;
    
    @PostConstruct
    public void init() {
        if (properties.isEnabled()) {
            log.info("动态MQ管理器初始化完成，当前MQ类型: {}", properties.getType());
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

        log.info("切换MQ类型: {} -> {}", getCurrentMqType(), mqType);

        // 分别切换生产者和消费者
        producerManager.switchMqType(mqType);
        consumerManager.switchMqType(mqType);

        log.info("MQ类型切换完成: {}", mqType);
    }
    
    /**
     * 获取当前MQ类型
     */
    public MqType getCurrentMqType() {
        // 优先返回生产者的MQ类型
        MqType producerType = producerManager.getCurrentMqType();
        if (producerType != null) {
            return producerType;
        }
        return consumerManager.getCurrentMqType();
    }

    /**
     * 检查MQ是否可用
     */
    public boolean isAvailable() {
        return properties.isEnabled() && producerManager.isAvailable() && consumerManager.isAvailable();
    }
    
    // ==================== MqProducer 接口实现 ====================

    @Override
    public boolean send(MqMessage message) {
        return producerManager.send(message);
    }

    @Override
    public boolean sendDelay(MqMessage message, long delayTime) {
        return producerManager.sendDelay(message, delayTime);
    }

    @Override
    public void sendAsync(MqMessage message, MqSendCallback callback) {
        producerManager.sendAsync(message, callback);
    }

    @Override
    public void sendOneWay(MqMessage message) {
        producerManager.sendOneWay(message);
    }

    @Override
    public boolean sendOrderly(MqMessage message, String orderKey) {
        return producerManager.sendOrderly(message, orderKey);
    }

    @Override
    public boolean sendTransaction(MqMessage message, String transactionId, io.github.kk01001.dynamic.mq.core.MqTransactionListener transactionListener) {
        return producerManager.sendTransaction(message, transactionId, transactionListener);
    }

    @Override
    public boolean sendBatch(MqMessage... messages) {
        return producerManager.sendBatch(messages);
    }

    @Override
    public String getProducerType() {
        return producerManager.getProducerType();
    }
    
    // ==================== MqConsumer 接口实现 ====================

    @Override
    public void start() {
        consumerManager.start();
    }

    @Override
    public void stop() {
        consumerManager.stop();
    }

    @Override
    public void subscribe(String topic, String tag, MqMessageHandler messageHandler) {
        consumerManager.subscribe(topic, tag, messageHandler);
    }

    @Override
    public void unsubscribe(String topic) {
        consumerManager.unsubscribe(topic);
    }

    @Override
    public String getConsumerType() {
        return consumerManager.getConsumerType();
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
        return producerManager.sendSimple(topic, payload);
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
        return producerManager.sendWithTag(topic, tag, payload);
    }

    /**
     * 订阅主题（不指定标签）
     *
     * @param topic 主题
     * @param messageHandler 消息处理器
     */
    public void subscribe(String topic, MqMessageHandler messageHandler) {
        consumerManager.subscribe(topic, messageHandler);
    }
    
    /**
     * 检查可用性
     */
    private void checkAvailable() {
        if (!isAvailable()) {
            throw new IllegalStateException("动态MQ不可用，请检查配置和初始化状态");
        }
    }

    // ==================== 消费者管理方法 ====================

    /**
     * 手动注册消费者
     *
     * @param consumer 消费者实例
     * @param topic 主题
     * @param tag 标签
     * @return 消费者ID
     */
    public String registerConsumer(DynamicMqConsumer consumer, String topic, String tag) {
        return consumerManager.registerConsumer(consumer, topic, tag);
    }

    /**
     * 手动注册消费者（无标签）
     *
     * @param consumer 消费者实例
     * @param topic 主题
     * @return 消费者ID
     */
    public String registerConsumer(DynamicMqConsumer consumer, String topic) {
        return consumerManager.registerConsumer(consumer, topic);
    }

    /**
     * 取消注册消费者
     *
     * @param consumerId 消费者ID
     * @return 是否成功
     */
    public boolean unregisterConsumer(String consumerId) {
        return consumerManager.unregisterConsumer(consumerId);
    }

    /**
     * 启动指定消费者
     *
     * @param consumerId 消费者ID
     */
    public void startConsumer(String consumerId) {
        consumerManager.startConsumer(consumerId);
    }

    /**
     * 停止指定消费者
     *
     * @param consumerId 消费者ID
     */
    public void stopConsumer(String consumerId) {
        consumerManager.stopConsumer(consumerId);
    }

    /**
     * 获取所有消费者注册信息
     *
     * @return 消费者列表
     */
    public List<ConsumerRegistration> getAllConsumers() {
        return consumerManager.getAllConsumers();
    }

    /**
     * 根据主题获取消费者
     *
     * @param topic 主题
     * @return 消费者列表
     */
    public List<ConsumerRegistration> getConsumersByTopic(String topic) {
        return consumerManager.getConsumersByTopic(topic);
    }


}
