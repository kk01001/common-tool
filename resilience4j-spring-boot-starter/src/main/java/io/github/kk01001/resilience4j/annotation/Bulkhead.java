package io.github.kk01001.resilience4j.annotation;

import io.github.kk01001.resilience4j.enums.FallbackStrategy;

import java.lang.annotation.*;

/**
 * 舱壁（隔离）注解
 *
 * @author kk01001
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Bulkhead {

    /**
     * 舱壁名称
     */
    String name() default "";

    /**
     * 最大并发调用数
     */
    int maxConcurrentCalls() default 25;

    /**
     * 最大等待时间（毫秒）
     */
    long maxWaitDuration() default 0;

    /**
     * 舱壁类型：SEMAPHORE（信号量）或 THREADPOOL（线程池）
     */
    Type type() default Type.SEMAPHORE;

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

    enum Type {
        SEMAPHORE,
        THREADPOOL
    }
}
