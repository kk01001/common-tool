package io.github.archer099.threadpool.thirdparty.adapter;

import io.github.archer099.threadpool.actuator.ThreadPoolMetrics;
import io.github.archer099.threadpool.alarm.ThreadPoolAlarmHandler;
import io.github.archer099.threadpool.thirdparty.ThirdPartyPoolType;
import io.github.archer099.threadpool.thirdparty.ThirdPartyThreadPoolConfig;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * 第三方线程池适配器抽象基类 (模板方法模式)
 * 
 * <p>
 * 提供公共的模板方法和默认实现，子类只需实现特定的钩子方法
 * 
 * @author archer099
 */
@Slf4j
public abstract class AbstractThirdPartyThreadPoolAdapter implements ThirdPartyThreadPoolAdapter {

    protected final String poolName;
    protected final ThirdPartyPoolType poolType;
    protected volatile ThirdPartyThreadPoolConfig config;

    @Setter
    protected ThreadPoolAlarmHandler alarmHandler;

    protected AbstractThirdPartyThreadPoolAdapter(String poolName, ThirdPartyPoolType poolType) {
        this.poolName = poolName;
        this.poolType = poolType;
    }

    @Override
    public String getPoolName() {
        return poolType.getCode() + ":" + poolName;
    }

    @Override
    public ThirdPartyPoolType getPoolType() {
        return poolType;
    }

    @Override
    public ThirdPartyThreadPoolConfig getConfig() {
        return config;
    }

    /**
     * 模板方法：收集指标
     * 1. 检查可用性
     * 2. 调用子类的具体收集逻辑
     * 3. 添加通用信息
     */
    @Override
    public ThreadPoolMetrics collectMetrics() {
        if (!isAvailable()) {
            log.warn("Third-party thread pool [{}] is not available, skip metrics collection", getPoolName());
            return null;
        }

        try {
            ThreadPoolMetrics metrics = doCollectMetrics();
            if (metrics != null) {
                // 确保 poolName 正确设置
                metrics.setPoolName(getPoolName());
            }
            return metrics;
        } catch (Exception e) {
            log.error("Failed to collect metrics for third-party thread pool [{}]", getPoolName(), e);
            return null;
        }
    }

    /**
     * 模板方法：更新配置
     * 1. 检查可用性
     * 2. 验证配置
     * 3. 调用子类的具体更新逻辑
     * 4. 更新本地配置缓存
     */
    @Override
    public void updateConfig(ThirdPartyThreadPoolConfig newConfig) {
        if (!isAvailable()) {
            throw new IllegalStateException("Third-party thread pool [" + getPoolName() + "] is not available");
        }

        if (newConfig == null) {
            throw new IllegalArgumentException("Config cannot be null");
        }

        try {
            String oldConfigStr = formatConfig(this.config);
            String newConfigStr = formatConfig(newConfig);

            log.info("Updating third-party thread pool [{}] config", getPoolName());
            doUpdateConfig(newConfig);
            this.config = newConfig;
            log.info("Successfully updated third-party thread pool [{}] config", getPoolName());

            // 发送配置变更告警
            if (alarmHandler != null) {
                alarmHandler.sendConfigChangeAlarm(getPoolName(), oldConfigStr, newConfigStr);
            }
        } catch (Exception e) {
            log.error("Failed to update config for third-party thread pool [{}]", getPoolName(), e);
            throw new RuntimeException("Failed to update thread pool config", e);
        }
    }

    /**
     * 格式化配置为字符串
     */
    private String formatConfig(ThirdPartyThreadPoolConfig config) {
        if (config == null) {
            return "null";
        }
        return String.format("core=%d, max=%d, queue=%d, keepAlive=%s",
                config.getMinThreads(),
                config.getMaxThreads(),
                config.getQueueCapacity(),
                config.getKeepAliveTime());
    }

    /**
     * 钩子方法：子类实现具体的指标收集逻辑
     */
    protected abstract ThreadPoolMetrics doCollectMetrics();

    /**
     * 钩子方法：子类实现具体的配置更新逻辑
     */
    protected abstract void doUpdateConfig(ThirdPartyThreadPoolConfig config);

    /**
     * 钩子方法：验证配置的有效性
     * 子类可以覆盖此方法进行自定义验证
     */
    protected void validateConfig(ThirdPartyThreadPoolConfig config) {
        if (config.getMaxThreads() != null && config.getMaxThreads() <= 0) {
            throw new IllegalArgumentException("maxThreads must be greater than 0");
        }
        if (config.getMinThreads() != null && config.getMinThreads() < 0) {
            throw new IllegalArgumentException("minThreads cannot be negative");
        }
        if (config.getMaxThreads() != null && config.getMinThreads() != null) {
            if (config.getMaxThreads() < config.getMinThreads()) {
                throw new IllegalArgumentException("maxThreads cannot be less than minThreads");
            }
        }
    }
}
