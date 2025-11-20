package io.github.kk01001.threadpool.thirdparty;

import lombok.Builder;
import lombok.Data;

/**
 * 第三方线程池配置模型
 * 
 * <p>统一的配置接口，不同的适配器根据需要使用其中的字段
 * 
 * @author kk01001
 */
@Data
@Builder
public class ThirdPartyThreadPoolConfig {
    
    /**
     * 线程池名称
     */
    private String poolName;
    
    /**
     * 最大线程数
     * 适用于：Tomcat (maxThreads), Dubbo (threads)
     */
    private Integer maxThreads;
    
    /**
     * 最小空闲线程数 / 核心线程数
     * 适用于：Tomcat (minSpareThreads), Dubbo (core threads)
     */
    private Integer minThreads;
    
    /**
     * 队列容量 / 等待队列大小
     * 适用于：Tomcat (acceptCount), Dubbo (queues)
     */
    private Integer queueCapacity;
    
    /**
     * 最大连接数
     * 适用于：Tomcat (maxConnections), Hikari (maximumPoolSize)
     */
    private Integer maxConnections;
    
    /**
     * 连接超时时间（毫秒）
     * 适用于：Tomcat (connectionTimeout), Hikari (connectionTimeout)
     */
    private Long connectionTimeout;
    
    /**
     * 线程存活时间（秒）
     * 适用于：Tomcat (keepAliveTimeout)
     */
    private Long keepAliveTime;
    
    /**
     * 是否允许核心线程超时
     */
    private Boolean allowCoreThreadTimeout;
    
    /**
     * 扩展配置（用于特定类型的线程池）
     * 使用 Map 存储额外的配置项
     */
    private java.util.Map<String, Object> extendedConfig;
    
    /**
     * 获取扩展配置值
     */
    @SuppressWarnings("unchecked")
    public <T> T getExtendedConfig(String key, Class<T> type) {
        if (extendedConfig == null) {
            return null;
        }
        Object value = extendedConfig.get(key);
        if (value == null) {
            return null;
        }
        return (T) value;
    }
    
    /**
     * 设置扩展配置值
     */
    public void putExtendedConfig(String key, Object value) {
        if (extendedConfig == null) {
            extendedConfig = new java.util.HashMap<>();
        }
        extendedConfig.put(key, value);
    }
}
