package io.github.kk01001.threadpool.handler;

import io.github.kk01001.threadpool.config.DynamicThreadPoolProperties;
import io.github.kk01001.threadpool.model.ThreadPoolConfig;
import io.github.kk01001.threadpool.registry.ThreadPoolRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.Map;

/**
 * 线程池配置刷新处理器
 * 监听配置变化并更新线程池
 *
 * @author kk01001
 */
@Slf4j
public class ThreadPoolRefreshHandler implements ApplicationListener<ContextRefreshedEvent> {

    private final ThreadPoolRegistry registry;
    private final DynamicThreadPoolProperties properties;

    public ThreadPoolRefreshHandler(ThreadPoolRegistry registry, DynamicThreadPoolProperties properties) {
        this.registry = registry;
        this.properties = properties;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.info("Application context refreshed, checking thread pool configurations");
        refreshAllConfigurations();
    }

    /**
     * 刷新所有线程池配置
     */
    public void refreshAllConfigurations() {
        Map<String, DynamicThreadPoolProperties.ThreadPoolConfigProperties> poolConfigs = properties.getPools();

        if (poolConfigs == null || poolConfigs.isEmpty()) {
            log.debug("No thread pool configurations to refresh");
            return;
        }

        poolConfigs.forEach((poolName, configProps) -> {
            try {
                refreshConfiguration(poolName, configProps);
            } catch (Exception e) {
                log.error("Failed to refresh configuration for thread pool [{}]", poolName, e);
            }
        });
    }

    /**
     * 刷新单个线程池配置
     */
    public void refreshConfiguration(String poolName,
                                     DynamicThreadPoolProperties.ThreadPoolConfigProperties configProps) {
        var wrapper = registry.getThreadPool(poolName);
        if (wrapper == null) {
            log.debug("Thread pool [{}] not found, skipping configuration refresh", poolName);
            return;
        }

        ThreadPoolConfig currentConfig = wrapper.getConfig();
        ThreadPoolConfig.ThreadPoolConfigBuilder newConfigBuilder = ThreadPoolConfig.builder()
                .poolName(poolName);

        // 合并配置
        newConfigBuilder.corePoolSize(
                configProps.getCorePoolSize() != null ?
                        configProps.getCorePoolSize() : currentConfig.getCorePoolSize());

        newConfigBuilder.maxPoolSize(
                configProps.getMaxPoolSize() != null ?
                        configProps.getMaxPoolSize() : currentConfig.getMaxPoolSize());

        newConfigBuilder.queueCapacity(
                configProps.getQueueCapacity() != null ?
                        configProps.getQueueCapacity() : currentConfig.getQueueCapacity());

        newConfigBuilder.keepAliveTime(
                configProps.getKeepAliveTime() != null ?
                        configProps.getKeepAliveTime() : currentConfig.getKeepAliveTime());

        newConfigBuilder.queueType(
                configProps.getQueueType() != null ?
                        configProps.getQueueType() : currentConfig.getQueueType());

        newConfigBuilder.threadNamePrefix(
                configProps.getThreadNamePrefix() != null ?
                        configProps.getThreadNamePrefix() : currentConfig.getThreadNamePrefix());

        newConfigBuilder.rejectedPolicyType(
                configProps.getRejectedPolicy() != null ?
                        configProps.getRejectedPolicy() : currentConfig.getRejectedPolicyType());

        newConfigBuilder.allowCoreThreadTimeout(
                configProps.getAllowCoreThreadTimeout() != null ?
                        configProps.getAllowCoreThreadTimeout() : currentConfig.getAllowCoreThreadTimeout());

        ThreadPoolConfig newConfig = newConfigBuilder.build();

        // 检查配置是否有变化
        if (!isConfigChanged(currentConfig, newConfig)) {
            log.debug("Thread pool [{}] configuration has not changed, skipping update", poolName);
            return;
        }

        // 更新配置
        wrapper.updateConfig(newConfig);
        log.info("Thread pool [{}] configuration refreshed successfully", poolName);
    }

    /**
     * 检查配置是否有变化
     */
    private boolean isConfigChanged(ThreadPoolConfig oldConfig, ThreadPoolConfig newConfig) {
        return !oldConfig.getCorePoolSize().equals(newConfig.getCorePoolSize()) ||
                !oldConfig.getMaxPoolSize().equals(newConfig.getMaxPoolSize()) ||
                !oldConfig.getKeepAliveTime().equals(newConfig.getKeepAliveTime()) ||
                !oldConfig.getAllowCoreThreadTimeout().equals(newConfig.getAllowCoreThreadTimeout());
    }
}
