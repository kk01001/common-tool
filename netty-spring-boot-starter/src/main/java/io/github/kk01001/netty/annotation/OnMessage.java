package io.github.kk01001.netty.annotation;

import java.lang.annotation.*;

/**
 * WebSocket消息处理方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OnMessage {

}