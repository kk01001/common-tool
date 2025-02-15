package io.github.kk01001.desensitize.annotation;

import io.github.kk01001.desensitize.enums.DesensitizeType;
import io.github.kk01001.desensitize.handler.DesensitizeHandler;

import java.lang.annotation.*;

/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description 数据脱敏注解
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

    /**
     * 开始脱敏的位置（包含）
     * 如果为负数，则从末尾开始计算
     * 默认值为0，表示从头开始
     */
    int startIndex() default 0;

    /**
     * 结束脱敏的位置（不包含）
     * 如果为负数，则从末尾开始计算
     * 默认值为-1，表示直到末尾
     */
    int endIndex() default -1;

    /**
     * 用于脱敏的字符
     * 默认为星号(*)
     */
    String maskChar() default "*";
} 