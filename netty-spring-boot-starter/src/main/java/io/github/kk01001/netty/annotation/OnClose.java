package io.github.kk01001.netty.annotation;

import java.lang.annotation.*;

/**
 * WebSocket连接关闭时的处理方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OnClose {
    
    /**
     * 执行顺序，数字越小优先级越高
     */
    int order() default 0;
} 