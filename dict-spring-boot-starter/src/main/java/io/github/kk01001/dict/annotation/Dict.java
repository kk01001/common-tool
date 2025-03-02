package io.github.kk01001.dict.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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

    /**
     * 指定表名
     */
    String table() default "";

    /**
     * 指定字段名
     */
    String field() default "";
} 