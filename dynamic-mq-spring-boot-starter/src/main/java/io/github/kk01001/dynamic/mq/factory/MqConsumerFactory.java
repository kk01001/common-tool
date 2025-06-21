package io.github.kk01001.dynamic.mq.factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.kk01001.dynamic.mq.config.DynamicMqProperties;
import io.github.kk01001.dynamic.mq.core.AbstractMqConsumer;
import io.github.kk01001.dynamic.mq.enums.MqType;
import io.github.kk01001.dynamic.mq.impl.kafka.KafkaMqConsumer;
import io.github.kk01001.dynamic.mq.impl.rabbitmq.RabbitMqConsumer;
import io.github.kk01001.dynamic.mq.impl.redis.RedisMqConsumer;
import io.github.kk01001.dynamic.mq.impl.rocketmq.RocketMqConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author linshiqiang
 * @date 2025-06-21 20:48:56
 * @description MQ消费者工厂类
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MqConsumerFactory {

    private final DynamicMqProperties properties;
    private final Map<MqType, AbstractMqConsumer> consumerCache = new ConcurrentHashMap<>();

    @Autowired(required = false)
    private RedissonClient redissonClient;

    @Autowired(required = false)
    private ObjectMapper objectMapper;
    
    /**
     * 获取消费者实例
     *
     * @param mqType MQ类型
     * @return 消费者实例
     */
    public AbstractMqConsumer getConsumer(MqType mqType) {
        return consumerCache.computeIfAbsent(mqType, this::createConsumer);
    }
    
    /**
     * 获取默认消费者实例
     *
     * @return 默认消费者实例
     */
    public AbstractMqConsumer getDefaultConsumer() {
        return getConsumer(properties.getType());
    }
    
    /**
     * 创建消费者实例
     *
     * @param mqType MQ类型
     * @return 消费者实例
     */
    private AbstractMqConsumer createConsumer(MqType mqType) {
        log.info("创建MQ消费者实例: {}", mqType);
        
        switch (mqType) {
            case ROCKETMQ:
                return createRocketMqConsumer();
            case RABBITMQ:
                return createRabbitMqConsumer();
            case KAFKA:
                return createKafkaConsumer();
            case REDIS:
                return createRedisConsumer();
            default:
                throw new IllegalArgumentException("不支持的MQ类型: " + mqType);
        }
    }
    
    /**
     * 创建RocketMQ消费者
     */
    private AbstractMqConsumer createRocketMqConsumer() {
        try {
            return new RocketMqConsumer(properties);
        } catch (Exception e) {
            log.error("创建RocketMQ消费者失败", e);
            throw new RuntimeException("创建RocketMQ消费者失败", e);
        }
    }
    
    /**
     * 创建RabbitMQ消费者
     */
    private AbstractMqConsumer createRabbitMqConsumer() {
        try {
            return new RabbitMqConsumer(properties);
        } catch (Exception e) {
            log.error("创建RabbitMQ消费者失败", e);
            throw new RuntimeException("创建RabbitMQ消费者失败", e);
        }
    }
    
    /**
     * 创建Kafka消费者
     */
    private AbstractMqConsumer createKafkaConsumer() {
        try {
            return new KafkaMqConsumer(properties);
        } catch (Exception e) {
            log.error("创建Kafka消费者失败", e);
            throw new RuntimeException("创建Kafka消费者失败", e);
        }
    }
    
    /**
     * 创建Redis消费者
     */
    private AbstractMqConsumer createRedisConsumer() {
        try {
            if (redissonClient == null) {
                throw new IllegalStateException("RedissonClient未配置，无法创建Redis消费者");
            }
            if (objectMapper == null) {
                objectMapper = new ObjectMapper();
            }
            return new RedisMqConsumer(properties, redissonClient, objectMapper);
        } catch (Exception e) {
            log.error("创建Redis消费者失败", e);
            throw new RuntimeException("创建Redis消费者失败", e);
        }
    }
    
    /**
     * 清理缓存
     */
    public void clearCache() {
        // 先停止所有消费者
        consumerCache.values().forEach(consumer -> {
            try {
                consumer.stop();
            } catch (Exception e) {
                log.error("停止消费者失败", e);
            }
        });
        
        consumerCache.clear();
        log.info("MQ消费者缓存已清理");
    }
    
    /**
     * 获取所有已创建的消费者
     */
    public Map<MqType, AbstractMqConsumer> getAllConsumers() {
        return new ConcurrentHashMap<>(consumerCache);
    }
}
