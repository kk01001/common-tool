package io.github.kk01001.netty.annotation;

import java.lang.annotation.*;

/**
 * WebSocket发生错误时的处理方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OnError {
    /**
     * 执行顺序，数字越小优先级越高
     */
    int order() default 0;
    
    /**
     * 是否继续传播异常
     */
    boolean propagate() default false;
} 