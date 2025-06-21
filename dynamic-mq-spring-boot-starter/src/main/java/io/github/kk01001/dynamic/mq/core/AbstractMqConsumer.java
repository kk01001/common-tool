package io.github.kk01001.dynamic.mq.core;

import io.github.kk01001.dynamic.mq.consumer.ConsumerRegistration;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author linshiqiang
 * @date 2025-06-21 20:48:56
 * @description MQ消费者抽象类，使用模板方法模式
 */
@Slf4j
public abstract class AbstractMqConsumer implements MqConsumer {
    
    /**
     * 消费者注册信息缓存
     */
    protected final Map<String, ConsumerRegistration> consumerRegistrations = new ConcurrentHashMap<>();
    
    /**
     * 消费者运行状态
     */
    protected volatile boolean running = false;
    
    /**
     * 启动消费者模板方法
     */
    @Override
    public final void start() {
        if (running) {
            log.warn("消费者已经启动");
            return;
        }
        
        try {
            log.info("启动{}消费者", getConsumerType());
            doStart();
            running = true;
            log.info("{}消费者启动成功", getConsumerType());
        } catch (Exception e) {
            log.error("启动{}消费者失败", getConsumerType(), e);
            throw new RuntimeException("启动消费者失败", e);
        }
    }
    
    /**
     * 停止消费者模板方法
     */
    @Override
    public final void stop() {
        if (!running) {
            log.warn("消费者已经停止");
            return;
        }
        
        try {
            log.info("停止{}消费者", getConsumerType());
            doStop();
            running = false;
            consumerRegistrations.clear();
            log.info("{}消费者停止成功", getConsumerType());
        } catch (Exception e) {
            log.error("停止{}消费者失败", getConsumerType(), e);
        }
    }
    
    /**
     * 订阅主题模板方法
     */
    @Override
    public final void subscribe(String topic, String tag, MqMessageHandler messageHandler) {
        try {
            validateSubscription(topic, tag, messageHandler);
            doSubscribe(topic, tag, messageHandler);
            log.info("{}订阅成功: topic={}, tag={}", getConsumerType(), topic, tag);
        } catch (Exception e) {
            log.error("{}订阅失败: topic={}, tag={}", getConsumerType(), topic, tag, e);
            throw new RuntimeException("订阅失败", e);
        }
    }
    
    /**
     * 取消订阅模板方法
     */
    @Override
    public final void unsubscribe(String topic) {
        try {
            doUnsubscribe(topic);
            log.info("{}取消订阅成功: topic={}", getConsumerType(), topic);
        } catch (Exception e) {
            log.error("{}取消订阅失败: topic={}", getConsumerType(), topic, e);
        }
    }
    
    /**
     * 注册消费者
     */
    public void registerConsumer(String consumerId, ConsumerRegistration registration) {
        consumerRegistrations.put(consumerId, registration);
        
        if (running && Boolean.TRUE.equals(registration.getAutoStartup())) {
            // 如果消费者已启动且配置为自动启动，立即订阅
            subscribe(registration.getTopic(), registration.getTag(), 
                    message -> {
                        try {
                            registration.getConsumer().consume(java.util.List.of(message), () -> {
                                log.debug("消息确认成功: consumerId={}", consumerId);
                            });
                            return true;
                        } catch (Exception e) {
                            log.error("消费者处理消息失败: consumerId={}", consumerId, e);
                            return false;
                        }
                    });
        }
    }
    
    /**
     * 取消注册消费者
     */
    public void unregisterConsumer(String consumerId) {
        ConsumerRegistration registration = consumerRegistrations.remove(consumerId);
        if (registration != null && running) {
            unsubscribe(registration.getTopic());
        }
    }
    
    /**
     * 获取消费者注册信息
     */
    public ConsumerRegistration getConsumerRegistration(String consumerId) {
        return consumerRegistrations.get(consumerId);
    }
    
    /**
     * 检查是否正在运行
     */
    public boolean isRunning() {
        return running;
    }
    
    /**
     * 校验订阅参数
     */
    protected void validateSubscription(String topic, String tag, MqMessageHandler messageHandler) {
        if (topic == null || topic.trim().isEmpty()) {
            throw new IllegalArgumentException("主题不能为空");
        }
        if (messageHandler == null) {
            throw new IllegalArgumentException("消息处理器不能为空");
        }
    }
    
    // 抽象方法，由子类实现
    
    /**
     * 执行启动
     */
    protected abstract void doStart();
    
    /**
     * 执行停止
     */
    protected abstract void doStop();
    
    /**
     * 执行订阅
     */
    protected abstract void doSubscribe(String topic, String tag, MqMessageHandler messageHandler);
    
    /**
     * 执行取消订阅
     */
    protected abstract void doUnsubscribe(String topic);
}
