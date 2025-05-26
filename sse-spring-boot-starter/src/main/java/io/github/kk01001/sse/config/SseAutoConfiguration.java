package io.github.kk01001.sse.config;

import io.github.kk01001.sse.controller.SseController;
import io.github.kk01001.sse.listener.RedisMessageListener;
import io.github.kk01001.sse.listener.RocketMqMessageListener;
import io.github.kk01001.sse.manager.SseConnectionManager;
import io.github.kk01001.sse.manager.impl.DefaultSseConnectionManager;
import io.github.kk01001.sse.publisher.SseMessagePublisher;
import io.github.kk01001.sse.publisher.impl.NoOpMessagePublisher;
import io.github.kk01001.sse.publisher.impl.RedisMessagePublisher;
import io.github.kk01001.sse.publisher.impl.RocketMqMessagePublisher;
import io.github.kk01001.sse.service.SseService;
import io.github.kk01001.sse.service.impl.DefaultSseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description SSE自动配置类
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(SseProperties.class)
@ConditionalOnProperty(prefix = "sse", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SseAutoConfiguration {

    /**
     * 配置SSE连接管理器
     */
    @Bean
    @ConditionalOnMissingBean
    public SseConnectionManager sseConnectionManager(SseProperties properties, SseMessagePublisher messagePublisher) {
        log.info("初始化SSE连接管理器");
        return new DefaultSseConnectionManager(properties, messagePublisher);
    }

    /**
     * 配置SSE服务
     */
    @Bean
    @ConditionalOnMissingBean
    public SseService sseService(SseConnectionManager connectionManager) {
        log.info("初始化SSE服务");
        return new DefaultSseService(connectionManager);
    }

    /**
     * 配置SSE控制器
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "sse", name = "controller.enabled", havingValue = "true", matchIfMissing = true)
    public SseController sseController(SseConnectionManager connectionManager) {
        log.info("初始化SSE控制器");
        return new SseController(connectionManager);
    }

    /**
     * 默认消息发布者配置
     */
    @Configuration
    @ConditionalOnProperty(prefix = "sse", name = "message-forward-type", havingValue = "none", matchIfMissing = true)
    static class DefaultMessagePublisherConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public SseMessagePublisher sseMessagePublisher() {
            log.info("初始化空实现消息发布者");
            return new NoOpMessagePublisher();
        }
    }

    /**
     * Redis消息发布者配置
     */
    @Configuration
    @ConditionalOnProperty(prefix = "sse", name = "message-forward-type", havingValue = "redis")
    @ConditionalOnBean(StringRedisTemplate.class)
    static class RedisMessagePublisherConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public SseMessagePublisher redisMessagePublisher(RedisTemplate<String, Object> redisTemplate, SseProperties properties) {
            log.info("初始化Redis消息发布者");
            return new RedisMessagePublisher(redisTemplate, properties);
        }

        @Bean
        @ConditionalOnMissingBean
        public RedisMessageListener redisMessageListener(SseConnectionManager connectionManager) {
            log.info("初始化Redis消息监听器");
            return new RedisMessageListener(connectionManager);
        }

        @Bean
        @ConditionalOnMissingBean
        public RedisMessageListenerContainer redisMessageListenerContainer(
                RedisConnectionFactory connectionFactory,
                RedisMessageListener messageListener,
                SseProperties properties) {
            log.info("初始化Redis消息监听容器");
            RedisMessageListenerContainer container = new RedisMessageListenerContainer();
            container.setConnectionFactory(connectionFactory);
            container.addMessageListener(messageListener, new ChannelTopic(properties.getRedis().getTopic()));
            return container;
        }
    }

    /**
     * RocketMQ消息发布者配置
     */
    @Configuration
    @ConditionalOnProperty(prefix = "sse", name = "message-forward-type", havingValue = "rocketmq")
    @ConditionalOnBean(RocketMQTemplate.class)
    static class RocketMqMessagePublisherConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public SseMessagePublisher rocketMqMessagePublisher(RocketMQTemplate rocketMQTemplate, SseProperties properties) {
            log.info("初始化RocketMQ消息发布者");
            return new RocketMqMessagePublisher(rocketMQTemplate, properties);
        }

        @Bean
        @ConditionalOnMissingBean
        public RocketMqMessageListener rocketMqMessageListener(SseConnectionManager connectionManager) {
            log.info("初始化RocketMQ消息监听器");
            return new RocketMqMessageListener(connectionManager);
        }
    }
}
