package io.github.archer099.threadpool.thirdparty.adapter;

import io.github.archer099.threadpool.actuator.ThreadPoolMetrics;
import io.github.archer099.threadpool.thirdparty.ThirdPartyPoolType;
import io.github.archer099.threadpool.thirdparty.ThirdPartyThreadPoolConfig;

/**
 * 第三方线程池适配器接口 (适配器模式)
 * 
 * <p>用于统一管理不同类型的第三方线程池（Tomcat、Dubbo、Hikari 等）
 * 
 * @author archer099
 */
public interface ThirdPartyThreadPoolAdapter {
    
    /**
     * 获取线程池名称（唯一标识）
     * 格式：{type}:{name}，例如 "tomcat:main"
     */
    String getPoolName();
    
    /**
     * 获取线程池类型
     */
    ThirdPartyPoolType getPoolType();
    
    /**
     * 判断该适配器是否可用
     * （例如：Tomcat 适配器在 Tomcat 环境下可用）
     */
    boolean isAvailable();
    
    /**
     * 收集线程池指标
     * 将第三方线程池的指标映射到标准的 ThreadPoolMetrics
     */
    ThreadPoolMetrics collectMetrics();
    
    /**
     * 更新线程池配置
     * 动态修改第三方线程池的参数
     * 
     * @param config 新的配置
     * @throws UnsupportedOperationException 如果该线程池不支持动态修改
     */
    void updateConfig(ThirdPartyThreadPoolConfig config);
    
    /**
     * 获取当前配置
     */
    ThirdPartyThreadPoolConfig getConfig();
    
    /**
     * 获取线程池的原生对象（用于特殊场景）
     * 返回 Object 类型，使用时需要强制类型转换
     */
    default Object getNativeThreadPool() {
        return null;
    }
}
