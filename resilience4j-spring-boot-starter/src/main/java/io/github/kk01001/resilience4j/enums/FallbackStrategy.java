package io.github.archer099.resilience4j.enums;

/**
 * 降级策略
 *
 * @author archer099
 */
public enum FallbackStrategy {
    /**
     * 抛出异常
     */
    EXCEPTION,
    
    /**
     * 调用降级方法
     */
    METHOD,
    
    /**
     * 返回默认值
     */
    DEFAULT_VALUE,
    
    /**
     * 返回null
     */
    NULL
}
