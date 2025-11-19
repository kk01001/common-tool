package io.github.kk01001.threadpool.registry;

import io.github.kk01001.threadpool.model.ThreadPoolConfig;
import io.github.kk01001.threadpool.model.ThreadPoolMetrics;
import io.github.kk01001.threadpool.wrapper.DynamicThreadPoolWrapper;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 线程池注册中心
 * 管理所有动态线程池
 *
 * @author kk01001
 */
@Slf4j
public class ThreadPoolRegistry {

    /**
     * 线程池映射表
     */
    private final Map<String, DynamicThreadPoolWrapper> threadPools = new ConcurrentHashMap<>();

    /**
     * 注册线程池
     */
    public void register(String poolName, DynamicThreadPoolWrapper wrapper) {
        if (threadPools.containsKey(poolName)) {
            log.warn("Thread pool [{}] already exists, will be replaced", poolName);
        }
        threadPools.put(poolName, wrapper);
        log.info("Thread pool [{}] registered successfully", poolName);
    }

    /**
     * 注销线程池
     */
    public void unregister(String poolName) {
        DynamicThreadPoolWrapper wrapper = threadPools.remove(poolName);
        if (wrapper != null) {
            wrapper.shutdown();
            log.info("Thread pool [{}] unregistered successfully", poolName);
        }
    }

    /**
     * 获取线程池
     */
    public DynamicThreadPoolWrapper getThreadPool(String poolName) {
        return threadPools.get(poolName);
    }

    /**
     * 获取所有线程池名称
     */
    public Collection<String> getAllPoolNames() {
        return threadPools.keySet();
    }

    /**
     * 获取所有线程池
     */
    public Collection<DynamicThreadPoolWrapper> getAllThreadPools() {
        return threadPools.values();
    }

    /**
     * 更新线程池配置
     */
    public void updateThreadPoolConfig(String poolName, ThreadPoolConfig config) {
        DynamicThreadPoolWrapper wrapper = threadPools.get(poolName);
        if (wrapper == null) {
            log.warn("Thread pool [{}] not found, cannot update config", poolName);
            return;
        }
        wrapper.updateConfig(config);
        log.info("Thread pool [{}] config updated", poolName);
    }

    /**
     * 收集所有线程池指标
     */
    public Map<String, ThreadPoolMetrics> collectAllMetrics() {
        Map<String, ThreadPoolMetrics> metricsMap = new ConcurrentHashMap<>();
        threadPools.forEach((name, wrapper) -> {
            try {
                ThreadPoolMetrics metrics = wrapper.collectMetrics();
                metricsMap.put(name, metrics);
            } catch (Exception e) {
                log.error("Failed to collect metrics for thread pool [{}]", name, e);
            }
        });
        return metricsMap;
    }

    /**
     * 获取线程池数量
     */
    public int size() {
        return threadPools.size();
    }

    /**
     * 关闭所有线程池
     */
    public void shutdownAll() {
        log.info("Shutting down all thread pools, total: {}", threadPools.size());
        threadPools.forEach((name, wrapper) -> {
            try {
                wrapper.shutdown();
                log.info("Thread pool [{}] shutdown", name);
            } catch (Exception e) {
                log.error("Failed to shutdown thread pool [{}]", name, e);
            }
        });
        threadPools.clear();
    }
}
