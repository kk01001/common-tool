package io.github.kk01001.dynamic.mq.manager;

import io.github.kk01001.dynamic.mq.config.DynamicMqProperties;
import io.github.kk01001.dynamic.mq.consumer.ConsumerRegistration;
import io.github.kk01001.dynamic.mq.consumer.ConsumerRegistry;
import io.github.kk01001.dynamic.mq.consumer.DynamicMqConsumer;
import io.github.kk01001.dynamic.mq.core.AbstractMqConsumer;
import io.github.kk01001.dynamic.mq.core.MqConsumer;
import io.github.kk01001.dynamic.mq.enums.MqType;
import io.github.kk01001.dynamic.mq.factory.MqConsumerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.List;

/**
 * @author linshiqiang
 * @date 2025-06-21 20:48:56
 * @description 动态MQ消费者管理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicMqConsumerManager implements MqConsumer {
    
    private final DynamicMqProperties properties;
    private final MqConsumerFactory consumerFactory;
    private final ConsumerRegistry consumerRegistry;
    
    private AbstractMqConsumer currentConsumer;
    
    @PostConstruct
    public void init() {
        if (properties.isEnabled()) {
            switchMqType(properties.getType());
            // 启动所有自动启动的消费者
            startAutoStartupConsumers();
            log.info("动态MQ消费者管理器初始化完成，当前MQ类型: {}", properties.getType());
        } else {
            log.info("动态MQ功能已禁用");
        }
    }
    
    @PreDestroy
    public void destroy() {
        if (currentConsumer != null) {
            currentConsumer.stop();
        }
        log.info("动态MQ消费者管理器资源清理完成");
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
        
        log.info("切换消费者MQ类型: {} -> {}", 
                currentConsumer != null ? currentConsumer.getConsumerType() : "none", 
                mqType);
        
        // 停止当前消费者
        if (currentConsumer != null) {
            currentConsumer.stop();
        }
        
        // 获取新的消费者
        currentConsumer = consumerFactory.getConsumer(mqType);
        
        // 启动新消费者
        currentConsumer.start();
        
        log.info("消费者MQ类型切换完成: {}", mqType);
    }
    
    /**
     * 获取当前MQ类型
     */
    public MqType getCurrentMqType() {
        if (currentConsumer == null) {
            return null;
        }
        return MqType.fromCode(currentConsumer.getConsumerType());
    }
    
    /**
     * 检查消费者是否可用
     */
    public boolean isAvailable() {
        return properties.isEnabled() && currentConsumer != null;
    }
    
    // ==================== MqConsumer 接口实现 ====================
    
    @Override
    public void start() {
        checkAvailable();
        currentConsumer.start();
    }
    
    @Override
    public void stop() {
        if (currentConsumer != null) {
            currentConsumer.stop();
        }
    }
    
    @Override
    public void subscribe(String topic, String tag, MqMessageHandler messageHandler) {
        checkAvailable();
        currentConsumer.subscribe(topic, tag, messageHandler);
    }
    
    @Override
    public void unsubscribe(String topic) {
        checkAvailable();
        currentConsumer.unsubscribe(topic);
    }
    
    @Override
    public String getConsumerType() {
        return currentConsumer != null ? currentConsumer.getConsumerType() : "none";
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
        checkAvailable();
        String consumerId = consumerRegistry.register(consumer, topic, tag);
        
        // 如果当前消费者已启动，立即启动这个消费者
        if (currentConsumer != null && currentConsumer.isRunning()) {
            startConsumer(consumerId);
        }
        
        return consumerId;
    }
    
    /**
     * 手动注册消费者（无标签）
     *
     * @param consumer 消费者实例
     * @param topic 主题
     * @return 消费者ID
     */
    public String registerConsumer(DynamicMqConsumer consumer, String topic) {
        return registerConsumer(consumer, topic, null);
    }
    
    /**
     * 取消注册消费者
     *
     * @param consumerId 消费者ID
     * @return 是否成功
     */
    public boolean unregisterConsumer(String consumerId) {
        // 先停止消费者
        stopConsumer(consumerId);
        
        // 然后取消注册
        return consumerRegistry.unregister(consumerId);
    }
    
    /**
     * 启动指定消费者
     *
     * @param consumerId 消费者ID
     */
    public void startConsumer(String consumerId) {
        checkAvailable();
        
        ConsumerRegistration registration = consumerRegistry.getRegistration(consumerId);
        if (registration == null) {
            log.warn("消费者不存在: {}", consumerId);
            return;
        }
        
        if (Boolean.TRUE.equals(registration.getStarted())) {
            log.warn("消费者已启动: {}", consumerId);
            return;
        }
        
        try {
            // 注册消费者到当前消费者实例
            currentConsumer.registerConsumer(consumerId, registration);
            
            // 使用当前消费者启动订阅
            currentConsumer.subscribe(
                    registration.getTopic(),
                    registration.getTag(),
                    message -> {
                        try {
                            registration.getConsumer().consume(List.of(message), () -> {
                                // 消息确认逻辑
                                log.debug("消息确认成功: consumerId={}, messageId={}", 
                                        consumerId, message.getMessageId());
                            });
                            return true;
                        } catch (Exception e) {
                            log.error("消费者处理消息失败: consumerId={}, error={}", 
                                    consumerId, e.getMessage(), e);
                            return false;
                        }
                    }
            );
            
            consumerRegistry.markAsStarted(consumerId);
            log.info("启动消费者成功: consumerId={}, topic={}", consumerId, registration.getTopic());
            
        } catch (Exception e) {
            log.error("启动消费者失败: consumerId={}, error={}", consumerId, e.getMessage(), e);
            throw new RuntimeException("启动消费者失败: " + consumerId, e);
        }
    }
    
    /**
     * 停止指定消费者
     *
     * @param consumerId 消费者ID
     */
    public void stopConsumer(String consumerId) {
        ConsumerRegistration registration = consumerRegistry.getRegistration(consumerId);
        if (registration == null) {
            log.warn("消费者不存在: {}", consumerId);
            return;
        }
        
        if (Boolean.FALSE.equals(registration.getStarted())) {
            log.warn("消费者已停止: {}", consumerId);
            return;
        }
        
        try {
            if (currentConsumer != null) {
                currentConsumer.unregisterConsumer(consumerId);
                currentConsumer.unsubscribe(registration.getTopic());
            }
            
            consumerRegistry.markAsStopped(consumerId);
            log.info("停止消费者成功: consumerId={}, topic={}", consumerId, registration.getTopic());
            
        } catch (Exception e) {
            log.error("停止消费者失败: consumerId={}, error={}", consumerId, e.getMessage(), e);
        }
    }
    
    /**
     * 获取所有消费者注册信息
     *
     * @return 消费者列表
     */
    public List<ConsumerRegistration> getAllConsumers() {
        return consumerRegistry.getAllRegistrations();
    }
    
    /**
     * 根据主题获取消费者
     *
     * @param topic 主题
     * @return 消费者列表
     */
    public List<ConsumerRegistration> getConsumersByTopic(String topic) {
        return consumerRegistry.getRegistrationsByTopic(topic);
    }
    
    /**
     * 订阅主题（不指定标签）
     *
     * @param topic 主题
     * @param messageHandler 消息处理器
     */
    public void subscribe(String topic, MqMessageHandler messageHandler) {
        subscribe(topic, null, messageHandler);
    }
    
    /**
     * 启动所有自动启动的消费者
     */
    private void startAutoStartupConsumers() {
        List<ConsumerRegistration> registrations = consumerRegistry.getAllRegistrations();
        
        for (ConsumerRegistration registration : registrations) {
            if (Boolean.TRUE.equals(registration.getAutoStartup())) {
                try {
                    startConsumer(registration.getConsumerId());
                } catch (Exception e) {
                    log.error("自动启动消费者失败: consumerId={}, error={}", 
                            registration.getConsumerId(), e.getMessage(), e);
                }
            }
        }
        
        log.info("自动启动消费者完成，总数: {}", registrations.size());
    }
    
    /**
     * 检查可用性
     */
    private void checkAvailable() {
        if (!isAvailable()) {
            throw new IllegalStateException("动态MQ消费者不可用，请检查配置和初始化状态");
        }
    }
}
