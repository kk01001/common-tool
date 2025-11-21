package io.github.kk01001.threadpool.actuator;

import io.github.kk01001.threadpool.custom.model.ThreadPoolConfig;
import io.github.kk01001.threadpool.custom.wrapper.DynamicThreadPoolWrapper;
import io.github.kk01001.threadpool.registry.ThreadPoolRegistry;
import io.github.kk01001.threadpool.thirdparty.adapter.ThirdPartyThreadPoolAdapter;
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
     * 查询所有线程池（包括第三方）
     * GET /actuator/dynamic-threadpool
     */
    @ReadOperation
    public Map<String, Object> getAllThreadPools() {
        Map<String, Object> result = new HashMap<>();
        Map<String, ThreadPoolMetrics> metricsMap = registry.collectAllMetricsIncludingThirdParty();

        result.put("total", metricsMap.size());
        result.put("pools", metricsMap);

        return result;
    }

    /**
     * 查询指定线程池（支持业务线程池和第三方线程池）
     * GET /actuator/dynamic-threadpool/{poolName}
     */
    @ReadOperation
    public ThreadPoolMetrics getThreadPool(@Selector String poolName) {
        // 先查找业务线程池
        DynamicThreadPoolWrapper wrapper = registry.getThreadPool(poolName);
        if (wrapper != null) {
            return wrapper.collectMetrics();
        }
        
        // 再查找第三方线程池
        var adapter = registry.getThirdPartyAdapter(poolName);
        if (adapter != null) {
            return adapter.collectMetrics();
        }
        
        throw new IllegalArgumentException("Thread pool not found: " + poolName);
    }

    /**
     * 更新线程池配置（支持业务线程池和第三方线程池）
     * POST /actuator/dynamic-threadpool/{poolName}
     */
    @WriteOperation
    public Map<String, Object> updateThreadPool(@Selector String poolName,
                                                Integer corePoolSize,
                                                Integer maxPoolSize,
                                                Integer queueCapacity,
                                                Long keepAliveSeconds) {
        // 先尝试业务线程池
        DynamicThreadPoolWrapper wrapper = registry.getThreadPool(poolName);
        if (wrapper != null) {
            return updateBusinessThreadPool(wrapper, poolName, corePoolSize, maxPoolSize, queueCapacity, keepAliveSeconds);
        }
        
        // 再尝试第三方线程池
        var adapter = registry.getThirdPartyAdapter(poolName);
        if (adapter != null) {
            return updateThirdPartyThreadPool(adapter, poolName, corePoolSize, maxPoolSize, queueCapacity, keepAliveSeconds);
        }
        
        throw new IllegalArgumentException("Thread pool not found: " + poolName);
    }
    
    /**
     * 更新业务线程池配置
     */
    private Map<String, Object> updateBusinessThreadPool(DynamicThreadPoolWrapper wrapper, String poolName,
                                                          Integer corePoolSize, Integer maxPoolSize,
                                                          Integer queueCapacity, Long keepAliveSeconds) {

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
    
    /**
     * 更新第三方线程池配置
     */
    private Map<String, Object> updateThirdPartyThreadPool(
            ThirdPartyThreadPoolAdapter adapter,
            String poolName, Integer corePoolSize, Integer maxPoolSize,
            Integer queueCapacity, Long keepAliveSeconds) {
        
        io.github.kk01001.threadpool.thirdparty.ThirdPartyThreadPoolConfig currentConfig = adapter.getConfig();
        
        // 构建新配置
        io.github.kk01001.threadpool.thirdparty.ThirdPartyThreadPoolConfig.ThirdPartyThreadPoolConfigBuilder builder = 
            io.github.kk01001.threadpool.thirdparty.ThirdPartyThreadPoolConfig.builder()
                .poolName(poolName);
        
        // 设置参数（使用新值或保持原值）
        if (maxPoolSize != null) {
            builder.maxThreads(maxPoolSize);
        } else if (currentConfig != null) {
            builder.maxThreads(currentConfig.getMaxThreads());
        }
        
        if (corePoolSize != null) {
            builder.minThreads(corePoolSize);  // 第三方线程池使用 minThreads
        } else if (currentConfig != null) {
            builder.minThreads(currentConfig.getMinThreads());
        }
        
        if (queueCapacity != null) {
            builder.queueCapacity(queueCapacity);
        } else if (currentConfig != null) {
            builder.queueCapacity(currentConfig.getQueueCapacity());
        }
        
        if (keepAliveSeconds != null) {
            builder.keepAliveTime(keepAliveSeconds);
        } else if (currentConfig != null) {
            builder.keepAliveTime(currentConfig.getKeepAliveTime());
        }
        
        io.github.kk01001.threadpool.thirdparty.ThirdPartyThreadPoolConfig newConfig = builder.build();
        adapter.updateConfig(newConfig);
        
        Map<String, Object> result = new HashMap<>();
        result.put("poolName", poolName);
        result.put("success", true);
        result.put("message", "Third-party thread pool configuration updated successfully");
        result.put("poolType", adapter.getPoolType().getDescription());
        
        // 返回配置信息
        Map<String, Object> oldConfigMap = new HashMap<>();
        if (currentConfig != null) {
            oldConfigMap.put("maxThreads", currentConfig.getMaxThreads());
            oldConfigMap.put("minThreads", currentConfig.getMinThreads());
            oldConfigMap.put("queueCapacity", currentConfig.getQueueCapacity());
            oldConfigMap.put("keepAliveTime", currentConfig.getKeepAliveTime());
        }
        
        Map<String, Object> newConfigMap = new HashMap<>();
        newConfigMap.put("maxThreads", newConfig.getMaxThreads());
        newConfigMap.put("minThreads", newConfig.getMinThreads());
        newConfigMap.put("queueCapacity", newConfig.getQueueCapacity());
        newConfigMap.put("keepAliveTime", newConfig.getKeepAliveTime());
        
        result.put("oldConfig", oldConfigMap);
        result.put("newConfig", newConfigMap);
        
        return result;
    }
}
