package io.github.kk01001.threadpool.custom.factory;

import io.github.kk01001.threadpool.custom.config.DynamicThreadPoolProperties;
import io.github.kk01001.threadpool.custom.model.ThreadPoolConfig;
import io.github.kk01001.threadpool.custom.wrapper.DynamicThreadPoolWrapper;

/**
 * 线程池工厂
 *
 * @author kk01001
 */
public class ThreadPoolFactory {

    private final DynamicThreadPoolProperties properties;

    public ThreadPoolFactory(DynamicThreadPoolProperties properties) {
        this.properties = properties;
    }

    /**
     * 创建线程池
     */
    public DynamicThreadPoolWrapper createThreadPool(String poolName) {
        ThreadPoolConfig config = buildConfig(poolName, null);
        return new DynamicThreadPoolWrapper(poolName, config);
    }

    /**
     * 创建线程池（带自定义配置）
     */
    public DynamicThreadPoolWrapper createThreadPool(String poolName, ThreadPoolConfig customConfig) {
        ThreadPoolConfig config = buildConfig(poolName, customConfig);
        return new DynamicThreadPoolWrapper(poolName, config);
    }

    /**
     * 构建线程池配置
     * 优先级：自定义配置 > 配置文件中的具体池配置 > 全局默认配置
     */
    private ThreadPoolConfig buildConfig(String poolName, ThreadPoolConfig customConfig) {
        DynamicThreadPoolProperties.GlobalConfig global = properties.getGlobal();
        DynamicThreadPoolProperties.ThreadPoolConfigProperties poolProps = properties.getPools().get(poolName);

        ThreadPoolConfig.ThreadPoolConfigBuilder builder = ThreadPoolConfig.builder()
                .poolName(poolName);

        // 核心线程数
        if (customConfig != null && customConfig.getCorePoolSize() != null) {
            builder.corePoolSize(customConfig.getCorePoolSize());
        } else if (poolProps != null && poolProps.getCorePoolSize() != null) {
            builder.corePoolSize(poolProps.getCorePoolSize());
        } else {
            builder.corePoolSize(global.getCorePoolSize());
        }

        // 最大线程数
        if (customConfig != null && customConfig.getMaxPoolSize() != null) {
            builder.maxPoolSize(customConfig.getMaxPoolSize());
        } else if (poolProps != null && poolProps.getMaxPoolSize() != null) {
            builder.maxPoolSize(poolProps.getMaxPoolSize());
        } else {
            builder.maxPoolSize(global.getMaxPoolSize());
        }

        // 队列容量
        if (customConfig != null && customConfig.getQueueCapacity() != null) {
            builder.queueCapacity(customConfig.getQueueCapacity());
        } else if (poolProps != null && poolProps.getQueueCapacity() != null) {
            builder.queueCapacity(poolProps.getQueueCapacity());
        } else {
            builder.queueCapacity(global.getQueueCapacity());
        }

        // 队列类型
        if (customConfig != null && customConfig.getQueueType() != null) {
            builder.queueType(customConfig.getQueueType());
        } else if (poolProps != null && poolProps.getQueueType() != null) {
            builder.queueType(poolProps.getQueueType());
        } else {
            builder.queueType(DynamicThreadPoolProperties.QueueType.LINKED_BLOCKING_QUEUE);
        }

        // 线程存活时间
        if (customConfig != null && customConfig.getKeepAliveTime() != null) {
            builder.keepAliveTime(customConfig.getKeepAliveTime());
        } else if (poolProps != null && poolProps.getKeepAliveTime() != null) {
            builder.keepAliveTime(poolProps.getKeepAliveTime());
        } else {
            builder.keepAliveTime(global.getKeepAliveTime());
        }

        // 线程名称前缀
        if (customConfig != null && customConfig.getThreadNamePrefix() != null) {
            builder.threadNamePrefix(customConfig.getThreadNamePrefix());
        } else if (poolProps != null && poolProps.getThreadNamePrefix() != null) {
            builder.threadNamePrefix(poolProps.getThreadNamePrefix());
        } else {
            builder.threadNamePrefix(global.getThreadNamePrefix() + poolName + "-");
        }

        // 拒绝策略
        if (customConfig != null && customConfig.getRejectedPolicyType() != null) {
            builder.rejectedPolicyType(customConfig.getRejectedPolicyType());
        } else if (poolProps != null && poolProps.getRejectedPolicy() != null) {
            builder.rejectedPolicyType(poolProps.getRejectedPolicy());
        } else {
            builder.rejectedPolicyType(DynamicThreadPoolProperties.RejectedPolicyType.ABORT_POLICY);
        }

        // 是否允许核心线程超时
        if (customConfig != null && customConfig.getAllowCoreThreadTimeout() != null) {
            builder.allowCoreThreadTimeout(customConfig.getAllowCoreThreadTimeout());
        } else if (poolProps != null && poolProps.getAllowCoreThreadTimeout() != null) {
            builder.allowCoreThreadTimeout(poolProps.getAllowCoreThreadTimeout());
        } else {
            builder.allowCoreThreadTimeout(global.getAllowCoreThreadTimeout());
        }

        // 是否启用 TTL
        if (customConfig != null && customConfig.getEnableTtl() != null) {
            builder.enableTtl(customConfig.getEnableTtl());
        } else if (poolProps != null && poolProps.getEnableTtl() != null) {
            builder.enableTtl(poolProps.getEnableTtl());
        } else {
            builder.enableTtl(global.getEnableTtl());
        }

        return builder.build();
    }
}
