package io.github.kk01001.netty.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.kk01001.netty.auth.DefaultWebSocketAuthenticator;
import io.github.kk01001.netty.auth.WebSocketAuthenticator;
import io.github.kk01001.netty.cluster.ClusterMessageHandler;
import io.github.kk01001.netty.cluster.DefaultClusterMessageHandler;
import io.github.kk01001.netty.cluster.RedisWebSocketClusterManager;
import io.github.kk01001.netty.cluster.WebSocketClusterManager;
import io.github.kk01001.netty.filter.MessageFilter;
import io.github.kk01001.netty.message.MessageDispatcher;
import io.github.kk01001.netty.registry.WebSocketEndpointRegistry;
import io.github.kk01001.netty.server.NettyWebSocketServer;
import io.github.kk01001.netty.session.WebSocketSessionManager;
import io.github.kk01001.netty.trace.MessageTracer;
import io.github.kk01001.netty.trace.MetricsMessageTracer;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

@Slf4j
@Configuration
@EnableConfigurationProperties(NettyWebSocketProperties.class)
public class NettyWebSocketAutoConfiguration {

    // @Bean
    // @ConditionalOnMissingBean
    // public RedisTemplate<String, String> stringRedisTemplate(RedisConnectionFactory connectionFactory) {
    //     RedisTemplate<String, String> template = new RedisTemplate<>();
    //     template.setConnectionFactory(connectionFactory);
    //     template.setKeySerializer(new StringRedisSerializer());
    //     template.setValueSerializer(new StringRedisSerializer());
    //     template.setHashKeySerializer(new StringRedisSerializer());
    //     template.setHashValueSerializer(new StringRedisSerializer());
    //     return template;
    // }
    
    @Bean
    @ConditionalOnMissingBean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        return container;
    }

    @Bean
    @ConditionalOnProperty(name = "netty.websocket.cluster.enabled", havingValue = "true", matchIfMissing = true)
    public MessageDispatcher messageDispatcher(
            ScheduledExecutorService scheduler,
            NettyWebSocketProperties properties,
            ApplicationEventPublisher eventPublisher) {
        return new WebSocketSessionManager(scheduler, properties, eventPublisher);
    }
    
    @Bean
    @ConditionalOnProperty(name = "netty.websocket.cluster.enabled", havingValue = "true")
    @ConditionalOnMissingBean(WebSocketClusterManager.class)
    public WebSocketClusterManager redisWebSocketClusterManager(
            RedisTemplate<String, String> redisTemplate,
            ObjectMapper objectMapper,
            NettyWebSocketProperties properties,
            RedisMessageListenerContainer listenerContainer,
            ClusterMessageHandler clusterMessageHandler,
            ScheduledExecutorService scheduler) {
        return new RedisWebSocketClusterManager(
                redisTemplate, objectMapper, properties, listenerContainer, clusterMessageHandler, scheduler);
    }
    
    @Bean
    public WebSocketEndpointRegistry webSocketEndpointRegistry(ApplicationContext applicationContext) {
        return new WebSocketEndpointRegistry(applicationContext);
    }
    
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "netty.websocket.auth-enabled", havingValue = "true")
    public WebSocketAuthenticator webSocketAuthenticator() {
        return new DefaultWebSocketAuthenticator();
    }
    
    @Bean
    @ConditionalOnMissingBean
    public List<WebSocketPipelineConfigurer> webSocketPipelineConfigurers() {
        return new ArrayList<>();
    }
    
    @Bean
    @ConditionalOnMissingBean
    public List<ChannelOptionCustomizer> channelOptionCustomizers() {
        return new ArrayList<>();
    }

    @Bean
    @ConditionalOnMissingBean
    public List<MessageFilter> messageFilters() {
        return new ArrayList<>();
    }

    @Bean
    @ConditionalOnMissingBean
    public NettyWebSocketServer nettyWebSocketServer(
            WebSocketEndpointRegistry registry,
            WebSocketSessionManager sessionManager,
            NettyWebSocketProperties properties,
            WebSocketAuthenticator authenticator,
            List<WebSocketPipelineConfigurer> pipelineConfigurers,
            List<ChannelOptionCustomizer> optionCustomizers,
            List<MessageFilter> messageFilters,
            MessageTracer messageTracer) {
        return new NettyWebSocketServer(
                registry, 
                sessionManager, 
                properties, 
                authenticator,
                pipelineConfigurers,
                optionCustomizers,
                messageFilters,
                messageTracer);
    }
    
    @Bean
    public ScheduledExecutorService webSocketScheduler() {
        return Executors.newScheduledThreadPool(1, 
                new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = new Thread(r);
                        thread.setName("websocket-scheduler-%d");
                        thread.setDaemon(true);
                        return thread;
                    }
                });
    }

    @Bean
    @ConditionalOnProperty(name = "netty.websocket.cluster.enabled", havingValue = "true")
    @ConditionalOnMissingBean
    public ClusterMessageHandler clusterMessageHandler(MessageDispatcher messageDispatcher) {
        return new DefaultClusterMessageHandler(messageDispatcher);
    }

    @Bean
    @ConditionalOnMissingBean
    public MessageTracer messageTracer(MeterRegistry registry, NettyWebSocketProperties properties) {
        return new MetricsMessageTracer(registry, properties);
    }
}