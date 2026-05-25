package io.github.archer099.resilience4j.annotation;

import io.github.archer099.resilience4j.enums.FallbackStrategy;

import java.lang.annotation.*;

/**
 * 限流器注解
 *
 * @author archer099
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimiter {

    /**
     * 限流器名称
     */
    String name() default "";

    /**
     * 限流周期内允许的请求数
     */
    int limitForPeriod() default 50;

    /**
     * 限流周期（纳秒）
     */
    long limitRefreshPeriod() default 500_000_000;

    /**
     * 等待许可的超时时间（毫秒）
     */
    long timeoutDuration() default 5000;

    /**
     * 降级策略
     */
    FallbackStrategy fallbackStrategy() default FallbackStrategy.EXCEPTION;

    /**
     * 降级方法名称
     */
    String fallbackMethod() default "";

    /**
     * 降级返回值
     */
    String fallbackValue() default "";
}
