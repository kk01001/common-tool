package io.github.kk01001.netty.annotation;

import java.lang.annotation.*;

/**
 * WebSocket端点注解
 * 标注在WebSocket处理类上
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WebSocketEndpoint {

    /**
     * WebSocket的路径
     */
    String path() default "/";
    
    /**
     * 子协议
     */
    String[] subprotocols() default {};
}
