package io.github.kk01001.common.dict.annotation;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Dict {
    
    /**
     * 字典类型/编码
     */
    String value();
    
    /**
     * 目标字段后缀，默认为Text
     */
    String suffix() default "Text";
} 