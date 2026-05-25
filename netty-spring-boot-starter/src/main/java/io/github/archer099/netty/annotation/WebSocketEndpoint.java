package io.github.archer099.netty.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author archer099
 * @date 2026-03-07 10:00:00
 * @description WebSocket端点注解，标注在WebSocket处理类上，支持指定路径
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WebSocketEndpoint {

    /**
     * WebSocket 路径，如 "/ws/chat"
     * 为空时使用全局配置的 netty.websocket.path
     */
    String value() default "";
}
