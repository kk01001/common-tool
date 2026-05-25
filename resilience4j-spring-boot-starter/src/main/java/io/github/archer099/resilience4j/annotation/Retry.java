package io.github.archer099.resilience4j.annotation;

import io.github.archer099.resilience4j.enums.FallbackStrategy;

import java.lang.annotation.*;

/**
 * 重试注解
 *
 * @author archer099
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Retry {

    /**
     * 重试器名称
     */
    String name() default "";

    /**
     * 最大重试次数
     */
    int maxAttempts() default 3;

    /**
     * 重试间隔（毫秒）
     */
    long waitDuration() default 500;

    /**
     * 需要重试的异常
     */
    Class<? extends Throwable>[] retryExceptions() default {Exception.class};

    /**
     * 忽略的异常（不重试）
     */
    Class<? extends Throwable>[] ignoreExceptions() default {};

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
