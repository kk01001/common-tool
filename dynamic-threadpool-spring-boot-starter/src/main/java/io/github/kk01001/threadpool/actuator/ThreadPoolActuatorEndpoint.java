package io.github.kk01001.threadpool.actuator;

import io.github.kk01001.threadpool.model.ThreadPoolConfig;
import io.github.kk01001.threadpool.model.ThreadPoolMetrics;
import io.github.kk01001.threadpool.registry.ThreadPoolRegistry;
import io.github.kk01001.threadpool.wrapper.DynamicThreadPoolWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;

import java.util.HashMap;
import java.util.Map;

/**
 * 动态线程池 Actuator 端点
 * 提供查询和更新线程池配置的 HTTP 接口
 *
 * @author kk01001
 */
@Slf4j
@Endpoint(id = "dynamic-threadpool")
public class ThreadPoolActuatorEndpoint {

    private final ThreadPoolRegistry registry;

    public ThreadPoolActuatorEndpoint(ThreadPoolRegistry registry) {
        this.registry = registry;
    }

    /**
     * 查询所有线程池
     * GET /actuator/dynamic-threadpool
     */
    @ReadOperation
    public Map<String, Object> getAllThreadPools() {
        Map<String, Object> result = new HashMap<>();
        Map<String, ThreadPoolMetrics> metricsMap = registry.collectAllMetrics();

        result.put("total", metricsMap.size());
        result.put("pools", metricsMap);

        return result;
    }

    /**
     * 查询指定线程池
     * GET /actuator/dynamic-threadpool/{poolName}
     */
    @ReadOperation
    public ThreadPoolMetrics getThreadPool(@Selector String poolName) {
        DynamicThreadPoolWrapper wrapper = registry.getThreadPool(poolName);
        if (wrapper == null) {
            throw new IllegalArgumentException("Thread pool not found: " + poolName);
        }
        return wrapper.collectMetrics();
    }

    /**
     * 更新线程池配置
     * POST /actuator/dynamic-threadpool/{poolName}
     */
    @WriteOperation
    public Map<String, Object> updateThreadPool(@Selector String poolName,
                                                Integer corePoolSize,
                                                Integer maxPoolSize,
                                                Integer queueCapacity,
                                                Long keepAliveSeconds) {
        DynamicThreadPoolWrapper wrapper = registry.getThreadPool(poolName);
        if (wrapper == null) {
            throw new IllegalArgumentException("Thread pool not found: " + poolName);
        }

        ThreadPoolConfig currentConfig = wrapper.getConfig();
        ThreadPoolConfig.ThreadPoolConfigBuilder newConfigBuilder = ThreadPoolConfig.builder()
                .poolName(poolName)
                .corePoolSize(corePoolSize != null ? corePoolSize : currentConfig.getCorePoolSize())
                .maxPoolSize(maxPoolSize != null ? maxPoolSize : currentConfig.getMaxPoolSize())
                .queueCapacity(queueCapacity != null ? queueCapacity : currentConfig.getQueueCapacity())
                .keepAliveTime(keepAliveSeconds != null ?
                        java.time.Duration.ofSeconds(keepAliveSeconds) : currentConfig.getKeepAliveTime())
                .queueType(currentConfig.getQueueType())
                .threadNamePrefix(currentConfig.getThreadNamePrefix())
                .rejectedPolicyType(currentConfig.getRejectedPolicyType())
                .allowCoreThreadTimeout(currentConfig.getAllowCoreThreadTimeout());

        ThreadPoolConfig newConfig = newConfigBuilder.build();
        wrapper.updateConfig(newConfig);

        Map<String, Object> result = new HashMap<>();
        result.put("poolName", poolName);
        result.put("success", true);
        result.put("message", "Thread pool configuration updated successfully");

        // 返回简化的配置信息，避免序列化问题
        Map<String, Object> oldConfigMap = new HashMap<>();
        oldConfigMap.put("corePoolSize", currentConfig.getCorePoolSize());
        oldConfigMap.put("maxPoolSize", currentConfig.getMaxPoolSize());
        oldConfigMap.put("queueCapacity", currentConfig.getQueueCapacity());
        oldConfigMap.put("keepAliveTime", currentConfig.getKeepAliveTime());
        oldConfigMap.put("rejectedPolicyType", currentConfig.getRejectedPolicyType());
        oldConfigMap.put("allowCoreThreadTimeout", currentConfig.getAllowCoreThreadTimeout());
        oldConfigMap.put("threadNamePrefix", currentConfig.getThreadNamePrefix());
        oldConfigMap.put("queueType", currentConfig.getQueueType());

        Map<String, Object> newConfigMap = new HashMap<>();
        newConfigMap.put("corePoolSize", newConfig.getCorePoolSize());
        newConfigMap.put("maxPoolSize", newConfig.getMaxPoolSize());
        newConfigMap.put("queueCapacity", newConfig.getQueueCapacity());
        newConfigMap.put("keepAliveTime", newConfig.getKeepAliveTime());
        newConfigMap.put("rejectedPolicyType", newConfig.getRejectedPolicyType());
        newConfigMap.put("allowCoreThreadTimeout", newConfig.getAllowCoreThreadTimeout());
        newConfigMap.put("threadNamePrefix", newConfig.getThreadNamePrefix());
        newConfigMap.put("queueType", newConfig.getQueueType());
        
        result.put("oldConfig", oldConfigMap);
        result.put("newConfig", newConfigMap);

        return result;
    }
}
