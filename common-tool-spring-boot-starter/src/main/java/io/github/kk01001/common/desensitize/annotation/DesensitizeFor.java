package io.github.kk01001.common.desensitize.annotation;

import io.github.kk01001.common.desensitize.enums.DesensitizeType;
import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DesensitizeFor {
    
    /**
     * 指定处理器对应的脱敏类型
     */
    DesensitizeType value();
} 