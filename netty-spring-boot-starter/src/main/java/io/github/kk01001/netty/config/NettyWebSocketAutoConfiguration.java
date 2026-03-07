package io.github.kk01001.netty.config;

import io.github.kk01001.netty.auth.DefaultWebSocketAuthenticator;
import io.github.kk01001.netty.auth.WebSocketAuthenticator;
import io.github.kk01001.netty.cluster.NoOpWebSocketClusterManager;
import io.github.kk01001.netty.cluster.WebSocketClusterManager;
import io.github.kk01001.netty.filter.MessageFilter;
import io.github.kk01001.netty.message.MessageDispatcher;
import io.github.kk01001.netty.registry.WebSocketEndpointRegistry;
import io.github.kk01001.netty.server.NettyWebSocketServer;
import io.github.kk01001.netty.session.WebSocketSessionManager;
import io.github.kk01001.netty.trace.MessageTracer;
import io.github.kk01001.netty.trace.MetricsMessageTracer;
import io.github.kk01001.netty.trace.NoOpMessageTracer;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author kk01001
 * @date 2026-03-07 10:00:00
 * @description Netty WebSocket 自动配置
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(NettyWebSocketProperties.class)
public class NettyWebSocketAutoConfiguration {

    // ====================== 核心 Bean（始终创建） ======================

    @Bean
    @ConditionalOnMissingBean(name = "webSocketScheduler")
    public ScheduledExecutorService webSocketScheduler() {
        AtomicInteger counter = new AtomicInteger(0);
        return Executors.newScheduledThreadPool(1, r -> {
            Thread thread = new Thread(r);
            thread.setName("ws-scheduler-" + counter.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        });
    }

    @Bean
    @ConditionalOnMissingBean
    public MessageDispatcher messageDispatcher(
            ScheduledExecutorService webSocketScheduler,
            NettyWebSocketProperties properties,
            ApplicationEventPublisher eventPublisher) {
        return new WebSocketSessionManager(webSocketScheduler, properties, eventPublisher);
    }

    @Bean
    @ConditionalOnMissingBean
    public WebSocketEndpointRegistry webSocketEndpointRegistry(
            ApplicationContext applicationContext,
            NettyWebSocketProperties properties) {
        return new WebSocketEndpointRegistry(applicationContext, properties);
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

    // ====================== 鉴权（条件创建） ======================

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "netty.websocket.auth-enabled", havingValue = "true")
    public WebSocketAuthenticator webSocketAuthenticator() {
        return new DefaultWebSocketAuthenticator();
    }

    // ====================== 消息追踪 ======================

    @Bean
    @ConditionalOnMissingBean(MessageTracer.class)
    @ConditionalOnBean(MeterRegistry.class)
    public MessageTracer metricsMessageTracer(MeterRegistry registry, NettyWebSocketProperties properties) {
        return new MetricsMessageTracer(registry, properties);
    }

    @Bean
    @ConditionalOnMissingBean(MessageTracer.class)
    public MessageTracer noOpMessageTracer() {
        return new NoOpMessageTracer();
    }

    // ====================== 非集群模式 Bean ======================

    @Bean
    @ConditionalOnMissingBean(WebSocketClusterManager.class)
    public WebSocketClusterManager noOpWebSocketClusterManager() {
        return new NoOpWebSocketClusterManager();
    }

    // ====================== 服务器 ======================

    @Bean
    @ConditionalOnMissingBean
    public NettyWebSocketServer nettyWebSocketServer(
            WebSocketEndpointRegistry registry,
            WebSocketSessionManager sessionManager,
            NettyWebSocketProperties properties,
            ObjectProvider<WebSocketAuthenticator> authenticatorProvider,
            List<WebSocketPipelineConfigurer> pipelineConfigurers,
            List<ChannelOptionCustomizer> optionCustomizers,
            List<MessageFilter> messageFilters,
            MessageTracer messageTracer,
            WebSocketClusterManager webSocketClusterManager) {
        return new NettyWebSocketServer(
                registry,
                sessionManager,
                properties,
                authenticatorProvider.getIfAvailable(),
                pipelineConfigurers,
                optionCustomizers,
                messageFilters,
                messageTracer,
                webSocketClusterManager);
    }
}
