package io.github.kk01001.threadpool.actuator;

import io.github.kk01001.threadpool.model.ThreadPoolConfig;
import io.github.kk01001.threadpool.model.ThreadPoolMetrics;
import io.github.kk01001.threadpool.registry.ThreadPoolRegistry;
import io.github.kk01001.threadpool.wrapper.DynamicThreadPoolWrapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;

import java.util.Collection;
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
        result.put("oldConfig", currentConfig);
        result.put("newConfig", newConfig);

        return result;
    }

    /**
     * 获取线程池列表
     * GET /actuator/dynamic-threadpool/list
     */
    @ReadOperation
    public PoolListResponse list() {
        Collection<String> poolNames = registry.getAllPoolNames();
        PoolListResponse response = new PoolListResponse();
        response.setTotal(poolNames.size());
        response.setPoolNames(poolNames);
        return response;
    }

    @Data
    public static class PoolListResponse {
        private Integer total;
        private Collection<String> poolNames;
    }
}
