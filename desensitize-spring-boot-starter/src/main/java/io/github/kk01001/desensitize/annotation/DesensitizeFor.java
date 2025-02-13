package io.github.kk01001.desensitize.annotation;

import io.github.kk01001.desensitize.enums.DesensitizeType;

import java.lang.annotation.*;

/**
 * @author kk01001
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