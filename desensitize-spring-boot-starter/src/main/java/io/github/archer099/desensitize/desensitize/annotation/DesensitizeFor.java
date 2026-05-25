package io.github.archer099.desensitize.desensitize.annotation;

import io.github.archer099.desensitize.desensitize.enums.DesensitizeType;

import java.lang.annotation.*;

/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DesensitizeFor {
    
    /**
     * 指定处理器对应的脱敏类型
     */
    DesensitizeType value();
} 