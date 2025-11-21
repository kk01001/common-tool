package io.github.kk01001.threadpool.custom.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 动态线程池注解
 * 标注在方法上，自动创建和管理线程池
 *
 * @author kk01001
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DynamicThreadPool {

    /**
     * 线程池名称（必填，全局唯一）
     */
    String poolName();

    /**
     * 核心线程数
     */
    int corePoolSize() default -1;

    /**
     * 最大线程数
     */
    int maxPoolSize() default -1;

    /**
     * 队列容量
     */
    int queueCapacity() default -1;

    /**
     * 线程存活时间（秒）
     */
    long keepAliveSeconds() default -1;

    /**
     * 线程名称前缀
     */
    String threadNamePrefix() default "";

    /**
     * 是否启用监控
     */
    boolean enableMonitor() default true;
}
