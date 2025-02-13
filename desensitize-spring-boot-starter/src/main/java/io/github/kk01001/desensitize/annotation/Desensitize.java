package io.github.kk01001.desensitize.annotation;

import io.github.kk01001.desensitize.enums.DesensitizeType;
import io.github.kk01001.desensitize.handler.DesensitizeHandler;

import java.lang.annotation.*;

/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Desensitize {
    
    /**
     * 脱敏类型
     */
    DesensitizeType type();
    
    /**
     * 自定义脱敏处理器类(当type=CUSTOM时生效)
     */
    Class<? extends DesensitizeHandler> handler() default DesensitizeHandler.class;
} 