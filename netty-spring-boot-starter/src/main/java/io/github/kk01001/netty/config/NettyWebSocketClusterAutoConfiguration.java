package io.github.kk01001.netty.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.kk01001.netty.cluster.ClusterMessageHandler;
import io.github.kk01001.netty.cluster.DefaultClusterMessageHandler;
import io.github.kk01001.netty.cluster.RedisWebSocketClusterManager;
import io.github.kk01001.netty.cluster.WebSocketClusterManager;
import io.github.kk01001.netty.event.WebSocketMessageEventListener;
import io.github.kk01001.netty.event.WebSocketSessionEventListener;
import io.github.kk01001.netty.message.MessageDispatcher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.util.concurrent.ScheduledExecutorService;

/**
 * @author kk01001
 * @date 2026-03-07 17:50:00
 * @description Netty WebSocket 集群模式自动配置，隔离 Redis 依赖避免类加载失败
 */
@Configuration
@ConditionalOnClass(RedisConnectionFactory.class)
@ConditionalOnProperty(name = "netty.websocket.cluster.enabled", havingValue = "true")
public class NettyWebSocketClusterAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(RedisMessageListenerContainer.class)
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        return container;
    }

    @Bean
    @ConditionalOnMissingBean
    public ClusterMessageHandler clusterMessageHandler(MessageDispatcher messageDispatcher) {
        return new DefaultClusterMessageHandler(messageDispatcher);
    }

    @Bean
    @ConditionalOnMissingBean(WebSocketClusterManager.class)
    public WebSocketClusterManager redisWebSocketClusterManager(
            RedisTemplate<String, String> redisTemplate,
            ObjectMapper objectMapper,
            NettyWebSocketProperties properties,
            RedisMessageListenerContainer listenerContainer,
            ClusterMessageHandler clusterMessageHandler,
            ScheduledExecutorService webSocketScheduler) {
        return new RedisWebSocketClusterManager(
                redisTemplate, objectMapper, properties, listenerContainer, clusterMessageHandler, webSocketScheduler);
    }

    @Bean
    public WebSocketSessionEventListener webSocketSessionEventListener(WebSocketClusterManager clusterManager) {
        return new WebSocketSessionEventListener(clusterManager);
    }

    @Bean
    public WebSocketMessageEventListener webSocketMessageEventListener(WebSocketClusterManager clusterManager) {
        return new WebSocketMessageEventListener(clusterManager);
    }
}
