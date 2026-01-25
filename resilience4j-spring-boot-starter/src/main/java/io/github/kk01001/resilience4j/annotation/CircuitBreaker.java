package io.github.kk01001.resilience4j.annotation;

import io.github.kk01001.resilience4j.enums.FallbackStrategy;

import java.lang.annotation.*;

/**
 * 熔断器注解
 *
 * @author kk01001
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CircuitBreaker {

    /**
     * 熔断器名称
     */
    String name() default "";

    /**
     * 失败率阈值（百分比）
     */
    float failureRateThreshold() default 50.0f;

    /**
     * 慢调用率阈值（百分比）
     */
    float slowCallRateThreshold() default 100.0f;

    /**
     * 慢调用时间阈值（毫秒）
     */
    long slowCallDurationThreshold() default 60000;

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
