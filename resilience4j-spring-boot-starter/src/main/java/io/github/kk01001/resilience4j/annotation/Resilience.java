package io.github.kk01001.resilience4j.annotation;

import java.lang.annotation.*;

/**
 * 组合注解，支持同时使用多个 Resilience4j 功能
 * 执行顺序：Retry -> CircuitBreaker -> RateLimiter -> TimeLimiter -> Bulkhead
 *
 * @author kk01001
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Resilience {

    /**
     * 熔断器配置
     */
    CircuitBreaker circuitBreaker() default @CircuitBreaker(name = "");

    /**
     * 限流器配置
     */
    RateLimiter rateLimiter() default @RateLimiter(name = "");

    /**
     * 重试配置
     */
    Retry retry() default @Retry(name = "");

    /**
     * 舱壁配置
     */
    Bulkhead bulkhead() default @Bulkhead(name = "");

    /**
     * 时间限制器配置
     */
    TimeLimiter timeLimiter() default @TimeLimiter(name = "");
}
