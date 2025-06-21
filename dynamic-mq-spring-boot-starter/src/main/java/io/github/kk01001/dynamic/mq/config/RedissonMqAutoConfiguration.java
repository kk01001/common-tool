package io.github.kk01001.dynamic.mq.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.kk01001.dynamic.mq.core.MqConsumer;
import io.github.kk01001.dynamic.mq.core.MqProducer;

import io.github.kk01001.dynamic.mq.impl.redis.RedisMqConsumer;
import io.github.kk01001.dynamic.mq.impl.redis.RedisMqProducer;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author linshiqiang
 * @date 2025-06-21 21:41:29
 * @description Redisson MQ自动配置
 */
@Slf4j
@Configuration
@ConditionalOnClass({Redisson.class, RedissonClient.class})
@ConditionalOnProperty(prefix = "dynamic.mq", name = {"enabled", "type"}, havingValue = "redis")
@ConditionalOnBean(RedissonClient.class)
@EnableConfigurationProperties(DynamicMqProperties.class)
public class RedissonMqAutoConfiguration {
    

    
    @Bean
    @ConditionalOnProperty(prefix = "dynamic.mq", name = "type", havingValue = "redis")
    public MqProducer redisMqProducer(DynamicMqProperties properties, RedissonClient redissonClient, ObjectMapper objectMapper) {
        log.info("创建Redisson MQ生产者");
        return new RedisMqProducer(properties, redissonClient, objectMapper);
    }

    @Bean
    @ConditionalOnProperty(prefix = "dynamic.mq", name = "type", havingValue = "redis")
    public MqConsumer redisMqConsumer(DynamicMqProperties properties, RedissonClient redissonClient, ObjectMapper objectMapper) {
        log.info("创建Redisson MQ消费者");
        return new RedisMqConsumer(properties, redissonClient, objectMapper);
    }
}
