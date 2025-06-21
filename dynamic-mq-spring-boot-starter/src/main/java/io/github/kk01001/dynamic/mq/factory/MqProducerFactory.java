package io.github.kk01001.dynamic.mq.factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.kk01001.dynamic.mq.config.DynamicMqProperties;
import io.github.kk01001.dynamic.mq.core.AbstractMqProducer;
import io.github.kk01001.dynamic.mq.enums.MqType;
import io.github.kk01001.dynamic.mq.impl.kafka.KafkaMqProducer;
import io.github.kk01001.dynamic.mq.impl.rabbitmq.RabbitMqProducer;
import io.github.kk01001.dynamic.mq.impl.redis.RedisMqProducer;
import io.github.kk01001.dynamic.mq.impl.rocketmq.RocketMqProducer;
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
 * @description MQ生产者工厂类
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MqProducerFactory {

    private final DynamicMqProperties properties;
    private final Map<MqType, AbstractMqProducer> producerCache = new ConcurrentHashMap<>();

    @Autowired(required = false)
    private RedissonClient redissonClient;

    @Autowired(required = false)
    private ObjectMapper objectMapper;
    
    /**
     * 获取生产者实例
     *
     * @param mqType MQ类型
     * @return 生产者实例
     */
    public AbstractMqProducer getProducer(MqType mqType) {
        return producerCache.computeIfAbsent(mqType, this::createProducer);
    }
    
    /**
     * 获取默认生产者实例
     *
     * @return 默认生产者实例
     */
    public AbstractMqProducer getDefaultProducer() {
        return getProducer(properties.getType());
    }
    
    /**
     * 创建生产者实例
     *
     * @param mqType MQ类型
     * @return 生产者实例
     */
    private AbstractMqProducer createProducer(MqType mqType) {
        log.info("创建MQ生产者实例: {}", mqType);
        
        switch (mqType) {
            case ROCKETMQ:
                return createRocketMqProducer();
            case RABBITMQ:
                return createRabbitMqProducer();
            case KAFKA:
                return createKafkaProducer();
            case REDIS:
                return createRedisProducer();
            default:
                throw new IllegalArgumentException("不支持的MQ类型: " + mqType);
        }
    }
    
    /**
     * 创建RocketMQ生产者
     */
    private AbstractMqProducer createRocketMqProducer() {
        try {
            return new RocketMqProducer(properties);
        } catch (Exception e) {
            log.error("创建RocketMQ生产者失败", e);
            throw new RuntimeException("创建RocketMQ生产者失败", e);
        }
    }
    
    /**
     * 创建RabbitMQ生产者
     */
    private AbstractMqProducer createRabbitMqProducer() {
        try {
            return new RabbitMqProducer(properties);
        } catch (Exception e) {
            log.error("创建RabbitMQ生产者失败", e);
            throw new RuntimeException("创建RabbitMQ生产者失败", e);
        }
    }
    
    /**
     * 创建Kafka生产者
     */
    private AbstractMqProducer createKafkaProducer() {
        try {
            return new KafkaMqProducer(properties);
        } catch (Exception e) {
            log.error("创建Kafka生产者失败", e);
            throw new RuntimeException("创建Kafka生产者失败", e);
        }
    }
    
    /**
     * 创建Redis生产者
     */
    private AbstractMqProducer createRedisProducer() {
        try {
            if (redissonClient == null) {
                throw new IllegalStateException("RedissonClient未配置，无法创建Redis生产者");
            }
            if (objectMapper == null) {
                objectMapper = new ObjectMapper();
            }
            return new RedisMqProducer(properties, redissonClient, objectMapper);
        } catch (Exception e) {
            log.error("创建Redis生产者失败", e);
            throw new RuntimeException("创建Redis生产者失败", e);
        }
    }
    
    /**
     * 清理缓存
     */
    public void clearCache() {
        producerCache.clear();
        log.info("MQ生产者缓存已清理");
    }
    
    /**
     * 获取所有已创建的生产者
     */
    public Map<MqType, AbstractMqProducer> getAllProducers() {
        return new ConcurrentHashMap<>(producerCache);
    }
}
