package io.github.archer099.example.threadpool.config;

import io.github.archer099.threadpool.custom.annotation.DynamicThreadPool;
import io.github.archer099.threadpool.custom.factory.ThreadPoolFactory;
import io.github.archer099.threadpool.registry.ThreadPoolRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;

/**
 * 线程池配置
 *
 * @author archer099
 */
@Configuration
public class ThreadPoolConfig {

    @Bean(name = "demo-pool")
    @DynamicThreadPool(
            corePoolSize = 8,
            maxPoolSize = 16,
            queueCapacity = 100,
            threadNamePrefix = "demo-",
            keepAliveSeconds = 60,
            poolName = "demo-pool")
    public Executor demoPool(ThreadPoolRegistry threadPoolRegistry, ThreadPoolFactory threadPoolFactory) {
        return null;
    }

    @Bean(name = "order-pool")
    @DynamicThreadPool(
            corePoolSize = 8,
            maxPoolSize = 16,
            queueCapacity = 200,
            threadNamePrefix = "demo-",
            keepAliveSeconds = 60,
            poolName = "order-pool")
    public Executor orderPool(ThreadPoolRegistry threadPoolRegistry, ThreadPoolFactory threadPoolFactory) {
        return null;
    }
}
