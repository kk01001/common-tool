package io.github.kk01001.common.desensitize.annotation;

import io.github.kk01001.common.desensitize.enums.DesensitizeType;
import io.github.kk01001.common.desensitize.handler.DesensitizeHandler;

import java.lang.annotation.*;

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