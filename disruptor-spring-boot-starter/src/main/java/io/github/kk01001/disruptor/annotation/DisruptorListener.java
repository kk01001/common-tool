package io.github.kk01001.disruptor.annotation;

import java.lang.annotation.*;

/**
 * @author kk01001
 * @date 2025-02-19 10:15:47
 * @description 消息监听注解，用于标记消息处理方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DisruptorListener {
    /**
     * 队列名称
     */
    String value();
    
    /**
     * 消费线程数
     */
    int threads() default 1;
    
    /**
     * 是否使用虚拟线程
     */
    boolean virtualThread() default true;
} 