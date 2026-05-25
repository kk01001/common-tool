package io.github.archer099.threadpool.custom.initializer;

import io.github.archer099.threadpool.custom.config.DynamicThreadPoolProperties;
import io.github.archer099.threadpool.custom.factory.ThreadPoolFactory;
import io.github.archer099.threadpool.custom.model.ThreadPoolConfig;
import io.github.archer099.threadpool.custom.wrapper.DynamicThreadPoolWrapper;
import io.github.archer099.threadpool.registry.ThreadPoolRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import java.util.Map;

/**
 * 线程池自动初始化器
 * 在应用启动时根据配置文件自动创建线程池
 *
 * @author archer099
 */
@Slf4j
public class ThreadPoolAutoInitializer implements ApplicationRunner {

    private final DynamicThreadPoolProperties properties;
    private final ThreadPoolFactory factory;
    private final ThreadPoolRegistry registry;

    public ThreadPoolAutoInitializer(DynamicThreadPoolProperties properties,
                                     ThreadPoolFactory factory,
                                     ThreadPoolRegistry registry) {
        this.properties = properties;
        this.factory = factory;
        this.registry = registry;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (properties.getPools() == null || properties.getPools().isEmpty()) {
            log.debug("No thread pools configured for auto-initialization");
            return;
        }

        int initializedCount = 0;
        for (Map.Entry<String, DynamicThreadPoolProperties.ThreadPoolConfigProperties> entry : properties.getPools().entrySet()) {
            String poolName = entry.getKey();
            DynamicThreadPoolProperties.ThreadPoolConfigProperties configProps = entry.getValue();

            // 检查是否启用自动初始化
            if (!Boolean.TRUE.equals(configProps.getAutoInit())) {
                log.debug("Thread pool [{}] auto-init is disabled, skipping", poolName);
                continue;
            }

            // 检查线程池是否已存在
            if (registry.getThreadPool(poolName) != null) {
                log.warn("Thread pool [{}] already exists, skipping auto-initialization", poolName);
                continue;
            }

            try {
                // 构建线程池配置
                ThreadPoolConfig config = buildThreadPoolConfig(poolName, configProps);

                // 创建线程池
                DynamicThreadPoolWrapper wrapper = factory.createThreadPool(poolName, config);

                // 注册线程池
                registry.register(poolName, wrapper);

                initializedCount++;
                log.info("Auto-initialized thread pool [{}] with config: corePoolSize={}, maxPoolSize={}, queueCapacity={}",
                        poolName, config.getCorePoolSize(), config.getMaxPoolSize(), config.getQueueCapacity());
            } catch (Exception e) {
                log.error("Failed to auto-initialize thread pool [{}]", poolName, e);
            }
        }

        if (initializedCount > 0) {
            log.info("Successfully auto-initialized {} thread pool(s)", initializedCount);
        }
    }

    /**
     * 构建线程池配置
     */
    private ThreadPoolConfig buildThreadPoolConfig(String poolName, DynamicThreadPoolProperties.ThreadPoolConfigProperties configProps) {
        ThreadPoolConfig.ThreadPoolConfigBuilder builder = ThreadPoolConfig.builder()
                .poolName(poolName);

        // 使用配置的值，如果没有配置则使用全局默认值
        DynamicThreadPoolProperties.GlobalConfig global = properties.getGlobal();

        builder.corePoolSize(configProps.getCorePoolSize() != null ?
                configProps.getCorePoolSize() : global.getCorePoolSize());

        builder.maxPoolSize(configProps.getMaxPoolSize() != null ?
                configProps.getMaxPoolSize() : global.getMaxPoolSize());

        builder.queueCapacity(configProps.getQueueCapacity() != null ?
                configProps.getQueueCapacity() : global.getQueueCapacity());

        builder.keepAliveTime(configProps.getKeepAliveTime() != null ?
                configProps.getKeepAliveTime() : global.getKeepAliveTime());

        builder.threadNamePrefix(configProps.getThreadNamePrefix() != null ?
                configProps.getThreadNamePrefix() : (poolName + "-"));

        builder.allowCoreThreadTimeout(configProps.getAllowCoreThreadTimeout() != null ?
                configProps.getAllowCoreThreadTimeout() : global.getAllowCoreThreadTimeout());

        builder.enableTtl(configProps.getEnableTtl() != null ?
                configProps.getEnableTtl() : global.getEnableTtl());

        // 队列类型
        if (configProps.getQueueType() != null) {
            builder.queueType(configProps.getQueueType());
        }

        // 拒绝策略
        if (configProps.getRejectedPolicy() != null) {
            builder.rejectedPolicyType(configProps.getRejectedPolicy());
        }

        return builder.build();
    }
}
