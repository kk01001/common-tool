package io.github.kk01001.netty.annotation;

import java.lang.annotation.*;

/**
 * WebSocket消息处理方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OnMessage {
    /**
     * 消息类型
     */
    Class<?> messageType() default String.class;
    
    /**
     * 是否需要解码
     */
    boolean decode() default false;
} 