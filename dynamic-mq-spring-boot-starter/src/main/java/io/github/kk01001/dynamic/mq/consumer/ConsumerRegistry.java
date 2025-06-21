package io.github.kk01001.dynamic.mq.consumer;

import io.github.kk01001.dynamic.mq.config.DynamicMqProperties;
import io.github.kk01001.dynamic.mq.enums.ConsumeMode;
import io.github.kk01001.dynamic.mq.enums.MessageModel;
import io.github.kk01001.dynamic.mq.enums.MessageSelectorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author linshiqiang
 * @date 2025-06-21 20:48:56
 * @description 消费者注册中心
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConsumerRegistry {
    
    private final DynamicMqProperties properties;
    private final Map<String, ConsumerRegistration> consumers = new ConcurrentHashMap<>();
    private final AtomicLong consumerIdGenerator = new AtomicLong(0);
    
    /**
     * 注册消费者
     *
     * @param registration 消费者注册信息
     * @return 消费者ID
     */
    public String register(ConsumerRegistration registration) {
        // 生成消费者ID
        if (!StringUtils.hasText(registration.getConsumerId())) {
            registration.setConsumerId(generateConsumerId());
        }
        
        // 设置默认值
        fillDefaultValues(registration);
        
        // 设置创建时间
        registration.setCreateTime(System.currentTimeMillis());
        registration.setStarted(false);
        
        // 注册消费者
        consumers.put(registration.getConsumerId(), registration);
        
        log.info("注册消费者成功: consumerId={}, topic={}, group={}", 
                registration.getConsumerId(), registration.getTopic(), registration.getGroup());
        
        return registration.getConsumerId();
    }
    
    /**
     * 便捷注册方法
     *
     * @param consumer 消费者实例
     * @param topic 主题
     * @param tag 标签
     * @return 消费者ID
     */
    public String register(DynamicMqConsumer consumer, String topic, String tag) {
        ConsumerRegistration registration = ConsumerRegistration.builder()
                .consumer(consumer)
                .topic(topic)
                .tag(tag)
                .build();
        return register(registration);
    }
    
    /**
     * 便捷注册方法（无标签）
     *
     * @param consumer 消费者实例
     * @param topic 主题
     * @return 消费者ID
     */
    public String register(DynamicMqConsumer consumer, String topic) {
        return register(consumer, topic, null);
    }
    
    /**
     * 取消注册消费者
     *
     * @param consumerId 消费者ID
     * @return 是否成功
     */
    public boolean unregister(String consumerId) {
        ConsumerRegistration registration = consumers.remove(consumerId);
        if (registration != null) {
            log.info("取消注册消费者: consumerId={}, topic={}", consumerId, registration.getTopic());
            return true;
        }
        return false;
    }
    
    /**
     * 获取消费者注册信息
     *
     * @param consumerId 消费者ID
     * @return 注册信息
     */
    public ConsumerRegistration getRegistration(String consumerId) {
        return consumers.get(consumerId);
    }
    
    /**
     * 获取所有消费者注册信息
     *
     * @return 所有注册信息
     */
    public List<ConsumerRegistration> getAllRegistrations() {
        return List.copyOf(consumers.values());
    }
    
    /**
     * 根据主题获取消费者
     *
     * @param topic 主题
     * @return 消费者列表
     */
    public List<ConsumerRegistration> getRegistrationsByTopic(String topic) {
        return consumers.values().stream()
                .filter(reg -> topic.equals(reg.getTopic()))
                .toList();
    }
    
    /**
     * 标记消费者为已启动
     *
     * @param consumerId 消费者ID
     */
    public void markAsStarted(String consumerId) {
        ConsumerRegistration registration = consumers.get(consumerId);
        if (registration != null) {
            registration.setStarted(true);
        }
    }
    
    /**
     * 标记消费者为已停止
     *
     * @param consumerId 消费者ID
     */
    public void markAsStopped(String consumerId) {
        ConsumerRegistration registration = consumers.get(consumerId);
        if (registration != null) {
            registration.setStarted(false);
        }
    }
    
    /**
     * 清空所有消费者
     */
    public void clear() {
        consumers.clear();
        log.info("清空所有消费者注册信息");
    }
    
    /**
     * 生成消费者ID
     */
    private String generateConsumerId() {
        return "consumer-" + consumerIdGenerator.incrementAndGet();
    }
    
    /**
     * 填充默认值
     */
    private void fillDefaultValues(ConsumerRegistration registration) {
        if (!StringUtils.hasText(registration.getMqType())) {
            registration.setMqType(properties.getType().getCode());
        }
        
        if (!StringUtils.hasText(registration.getGroup())) {
            registration.setGroup(properties.getConsumer().getGroupName());
        }
        
        if (registration.getPullBatchSize() == null || registration.getPullBatchSize() <= 0) {
            registration.setPullBatchSize(32);
        }
        
        if (registration.getConsumeMessageBatchMaxSize() == null || registration.getConsumeMessageBatchMaxSize() <= 0) {
            registration.setConsumeMessageBatchMaxSize(properties.getConsumer().getConsumeMessageBatchMaxSize());
        }
        
        if (registration.getConsumeThreadMin() == null || registration.getConsumeThreadMin() <= 0) {
            registration.setConsumeThreadMin(properties.getConsumer().getConsumeThreadMin());
        }
        
        if (registration.getConsumeThreadMax() == null || registration.getConsumeThreadMax() <= 0) {
            registration.setConsumeThreadMax(properties.getConsumer().getConsumeThreadMax());
        }
        
        if (registration.getConsumeTimeout() == null || registration.getConsumeTimeout() <= 0) {
            registration.setConsumeTimeout(900000L); // 15分钟
        }
        
        if (registration.getConsumeMode() == null || registration.getConsumeMode() == ConsumeMode.UNSET) {
            registration.setConsumeMode(ConsumeMode.CONCURRENTLY);
        }
        
        if (registration.getMessageModel() == null || registration.getMessageModel() == MessageModel.UNSET) {
            registration.setMessageModel(MessageModel.CLUSTERING);
        }
        
        if (registration.getSelectorType() == null || registration.getSelectorType() == MessageSelectorType.UNSET) {
            registration.setSelectorType(MessageSelectorType.TAG);
        }
        
        if (!StringUtils.hasText(registration.getSelectorExpression())) {
            registration.setSelectorExpression("*");
        }
        
        if (registration.getEnableMsgTrace() == null) {
            registration.setEnableMsgTrace(true);
        }
        
        if (registration.getAutoStartup() == null) {
            registration.setAutoStartup(true);
        }
    }
}
