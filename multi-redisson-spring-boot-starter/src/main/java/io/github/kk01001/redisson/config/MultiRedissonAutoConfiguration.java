package io.github.kk01001.redisson.config;

import io.github.kk01001.redisson.circuitbreaker.CircuitBreakerState;
import io.github.kk01001.redisson.circuitbreaker.DualWriteCircuitBreaker;
import io.github.kk01001.redisson.factory.RedissonClientFactory;
import io.github.kk01001.redisson.factory.RedissonClientFactoryImpl;
import io.github.kk01001.redisson.health.RedissonHealthIndicator;
import io.github.kk01001.redisson.holder.RedissonClientHolder;
import io.github.kk01001.redisson.monitor.CircuitBreakerEndpoint;
import io.github.kk01001.redisson.monitor.DualWriteEndpoint;
import io.github.kk01001.redisson.monitor.DualWriteMetrics;
import io.github.kk01001.redisson.properties.MultiRedissonProperties;
import io.github.kk01001.redisson.properties.RedissonInstanceProperties;
import io.github.kk01001.redisson.recovery.DualWriteRecoveryHandler;
import io.github.kk01001.redisson.retry.DefaultDualWriteFailureHandler;
import io.github.kk01001.redisson.retry.DualWriteFailureHandler;
import io.github.kk01001.redisson.retry.DualWriteOverflowHandler;
import io.github.kk01001.redisson.retry.DualWriteRejectedHandler;
import io.github.kk01001.redisson.template.MultiRedissonTemplate;
import jakarta.annotation.PreDestroy;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author kk01001
 * @date 2026-01-15 10:00:00
 * @description Redisson 多集群自动配置类
 */
@AutoConfiguration
@ConditionalOnClass(RedissonClient.class)
@ConditionalOnProperty(prefix = "redisson.multi", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(MultiRedissonProperties.class)
public class MultiRedissonAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(MultiRedissonAutoConfiguration.class);

    private final MultiRedissonProperties properties;
    private final Map<String, RedissonClient> clients = new LinkedHashMap<>();
    private ThreadPoolExecutor dualWriteExecutor;
    private DualWriteMetrics dualWriteMetrics;
    private DualWriteFailureHandler failureHandler;

    public MultiRedissonAutoConfiguration(MultiRedissonProperties properties) {
        this.properties = properties;
    }

    /**
     * 创建双写监控指标
     */
    @Bean
    @ConditionalOnMissingBean
    public DualWriteMetrics dualWriteMetrics() {
        this.dualWriteMetrics = new DualWriteMetrics();
        return this.dualWriteMetrics;
    }

    /**
     * 创建 RedissonClient 工厂
     */
    @Bean
    @ConditionalOnMissingBean
    public RedissonClientFactory redissonClientFactory() {
        return new RedissonClientFactoryImpl();
    }

    /**
     * 创建双写失败处理器
     * <p>
     * 用户可自定义实现 {@link DualWriteFailureHandler} 接口来替换默认实现。
     * 例如基于 MQ、数据库等持久化方案。
     * </p>
     */
    @Bean
    @ConditionalOnProperty(prefix = "redisson.multi", name = "dual-write-enabled", havingValue = "true")
    @ConditionalOnMissingBean(DualWriteFailureHandler.class)
    public DualWriteFailureHandler dualWriteFailureHandler(DualWriteCircuitBreaker circuitBreaker,
                                                           DualWriteMetrics metrics,
                                                           ObjectProvider<DualWriteOverflowHandler> overflowHandlerProvider) {
        MultiRedissonProperties.RetryConfig retryConfig = properties.getRetry();
        if (!retryConfig.isEnabled()) {
            log.info("Dual write retry is disabled");
            return null;
        }

        DualWriteOverflowHandler overflowHandler = overflowHandlerProvider.getIfAvailable();
        DefaultDualWriteFailureHandler handler = new DefaultDualWriteFailureHandler(
                retryConfig, circuitBreaker, metrics, overflowHandler);
        handler.start();
        this.failureHandler = handler;

        log.info("DefaultDualWriteFailureHandler created, queueCapacity={}, maxRetryCount={}, overflowHandler={}",
                retryConfig.getQueueCapacity(), retryConfig.getMaxRetryCount(),
                overflowHandler != null ? overflowHandler.getClass().getSimpleName() : "none");
        return handler;
    }

    /**
     * 创建双写线程池
     */
    @Bean(name = "dualWriteExecutor")
    @ConditionalOnProperty(prefix = "redisson.multi", name = "dual-write-enabled", havingValue = "true")
    @ConditionalOnMissingBean(name = "dualWriteExecutor")
    public ExecutorService dualWriteExecutor(DualWriteMetrics metrics,
                                             ObjectProvider<DualWriteFailureHandler> failureHandlerProvider) {
        MultiRedissonProperties.DualWriteThreadPool config = properties.getDualWriteThreadPool();

        ThreadFactory threadFactory = new ThreadFactory() {
            private final AtomicInteger counter = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName(config.getThreadNamePrefix() + counter.getAndIncrement());
                thread.setDaemon(true);
                return thread;
            }
        };

        DualWriteFailureHandler failureHandler = failureHandlerProvider.getIfAvailable();

        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                config.getCorePoolSize(),
                config.getMaxPoolSize(),
                config.getKeepAliveSeconds(),
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(config.getQueueCapacity()),
                threadFactory,
                new DualWriteRejectedHandler(metrics, failureHandler)
        );

        executor.allowCoreThreadTimeOut(config.isAllowCoreThreadTimeOut());

        this.dualWriteExecutor = executor;

        metrics.setExecutor(executor);

        log.info("Dual write executor created, corePoolSize: {}, maxPoolSize: {}, queueCapacity: {}",
                config.getCorePoolSize(), config.getMaxPoolSize(), config.getQueueCapacity());

        return executor;
    }

    /**
     * 创建 RedissonClient 持有者
     */
    @Bean
    @ConditionalOnMissingBean
    public RedissonClientHolder redissonClientHolder(RedissonClientFactory factory) {
        Map<String, RedissonInstanceProperties> instances = properties.getInstances();

        if (instances == null || instances.isEmpty()) {
            throw new IllegalArgumentException("No Redisson instances configured. " +
                    "Please configure at least one instance under 'redisson.multi.instances'");
        }

        String primaryName = properties.getPrimary();
        if (!instances.containsKey(primaryName)) {
            throw new IllegalArgumentException("Primary instance '" + primaryName +
                    "' not found in configured instances: " + instances.keySet());
        }

        log.info("Initializing {} RedissonClient instances...", instances.size());

        for (Map.Entry<String, RedissonInstanceProperties> entry : instances.entrySet()) {
            String name = entry.getKey();
            RedissonInstanceProperties instanceProps = entry.getValue();

            try {
                RedissonClient client = factory.create(name, instanceProps);
                clients.put(name, client);
                log.info("RedissonClient '{}' created successfully", name);
            } catch (Exception e) {
                log.error("Failed to create RedissonClient '{}'", name, e);
                throw new RuntimeException("Failed to create RedissonClient: " + name, e);
            }
        }

        return new RedissonClientHolder(clients, primaryName);
    }

    /**
     * 创建主 RedissonClient Bean
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean(RedissonClient.class)
    public RedissonClient redissonClient(RedissonClientHolder holder) {
        return holder.getPrimary();
    }

    /**
     * 创建双写熔断器（单例）
     */
    @Bean
    @ConditionalOnMissingBean
    public DualWriteCircuitBreaker dualWriteCircuitBreaker(ObjectProvider<DualWriteRecoveryHandler> recoveryHandlerProvider) {
        DualWriteCircuitBreaker circuitBreaker = new DualWriteCircuitBreaker(properties.getCircuitBreaker());

        DualWriteRecoveryHandler recoveryHandler = recoveryHandlerProvider.getIfAvailable();
        if (recoveryHandler != null) {
            circuitBreaker.addStateChangeListener((from, to) -> {
                if (to == CircuitBreakerState.CLOSED && from != CircuitBreakerState.CLOSED) {
                    long openTimestamp = circuitBreaker.getStats().openTimestamp();
                    long now = System.currentTimeMillis();
                    DualWriteRecoveryHandler.RecoveryContext context = new DualWriteRecoveryHandler.RecoveryContext(
                            from, openTimestamp, now, openTimestamp > 0 ? now - openTimestamp : 0
                    );
                    try {
                        recoveryHandler.onRecovery(context);
                    } catch (Exception e) {
                        log.error("Recovery handler failed", e);
                    }
                }
            });
            log.info("DualWriteRecoveryHandler registered with circuit breaker");
        }

        return circuitBreaker;
    }

    /**
     * 创建多集群操作模板
     */
    @Bean
    @ConditionalOnMissingBean
    public MultiRedissonTemplate multiRedissonTemplate(RedissonClientHolder holder,
                                                       @Qualifier("dualWriteExecutor") ObjectProvider<ExecutorService> dualWriteExecutorProvider,
                                                       DualWriteMetrics metrics,
                                                       DualWriteCircuitBreaker circuitBreaker,
                                                       ObjectProvider<DualWriteFailureHandler> failureHandlerProvider) {
        ExecutorService dualWriteExecutor = dualWriteExecutorProvider.getIfAvailable();
        DualWriteFailureHandler failureHandler = failureHandlerProvider.getIfAvailable();
        return new MultiRedissonTemplate(holder, properties, dualWriteExecutor, metrics, circuitBreaker, failureHandler);
    }

    /**
     * 创建双写监控端点
     */
    @Bean
    @ConditionalOnClass(name = "org.springframework.boot.actuate.endpoint.annotation.Endpoint")
    @ConditionalOnAvailableEndpoint(endpoint = DualWriteEndpoint.class)
    @ConditionalOnMissingBean
    public DualWriteEndpoint dualWriteEndpoint(DualWriteMetrics metrics,
                                                  DualWriteCircuitBreaker circuitBreaker,
                                                  ObjectProvider<DualWriteFailureHandler> failureHandlerProvider) {
        return new DualWriteEndpoint(metrics, circuitBreaker, failureHandlerProvider.getIfAvailable());
    }

    /**
     * 创建 Redisson 健康检查指示器
     */
    @Bean
    @ConditionalOnClass(name = "org.springframework.boot.actuate.health.HealthIndicator")
    @ConditionalOnMissingBean(name = "redissonHealthIndicator")
    public RedissonHealthIndicator redissonHealthIndicator(RedissonClientHolder holder) {
        return new RedissonHealthIndicator(holder);
    }

    /**
     * 创建熔断器操作端点
     */
    @Bean
    @ConditionalOnClass(name = "org.springframework.boot.actuate.endpoint.annotation.Endpoint")
    @ConditionalOnAvailableEndpoint(endpoint = CircuitBreakerEndpoint.class)
    @ConditionalOnMissingBean
    public CircuitBreakerEndpoint circuitBreakerEndpoint(DualWriteCircuitBreaker circuitBreaker) {
        return new CircuitBreakerEndpoint(circuitBreaker);
    }

    /**
     * 关闭所有客户端
     */
    @PreDestroy
    public void destroy() {
        if (failureHandler != null) {
            log.info("Shutting down dual write failure handler...");
            failureHandler.shutdown();
        }

        if (dualWriteExecutor != null) {
            log.info("Shutting down dual write executor...");
            dualWriteExecutor.shutdown();
            try {
                if (!dualWriteExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                    dualWriteExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                dualWriteExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        log.info("Shutting down all RedissonClients...");
        for (Map.Entry<String, RedissonClient> entry : clients.entrySet()) {
            try {
                if (!entry.getValue().isShutdown()) {
                    entry.getValue().shutdown();
                    log.info("RedissonClient '{}' shutdown successfully", entry.getKey());
                }
            } catch (Exception e) {
                log.error("Failed to shutdown RedissonClient '{}'", entry.getKey(), e);
            }
        }
    }
}
