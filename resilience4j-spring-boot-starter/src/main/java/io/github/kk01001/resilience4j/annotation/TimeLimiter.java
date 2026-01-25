package io.github.kk01001.resilience4j.annotation;

import io.github.kk01001.resilience4j.enums.FallbackStrategy;

import java.lang.annotation.*;

/**
 * 时间限制器注解
 *
 * @author kk01001
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TimeLimiter {

    /**
     * 时间限制器名称
     */
    String name() default "";

    /**
     * 超时时间（毫秒）
     */
    long timeoutDuration() default 1000;

    /**
     * 是否取消运行中的Future
     */
    boolean cancelRunningFuture() default true;

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
