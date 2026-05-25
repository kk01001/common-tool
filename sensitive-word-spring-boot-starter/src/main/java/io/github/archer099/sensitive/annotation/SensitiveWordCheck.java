package io.github.archer099.sensitive.annotation;

import io.github.archer099.sensitive.core.HandleType;
import io.github.archer099.sensitive.core.MatchType;

import java.lang.annotation.*;

/**
 * @author archer099
 * @date 2026-01-18 13:02:31
 * @description 敏感词检测注解，用于标记需要进行敏感词检测的方法参数或字段
 */
@Target({ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SensitiveWordCheck {
    
    /**
     * 检测的字段名称（用于对象类型参数）
     * 为空时检测整个字符串参数或对象的所有字符串字段
     */
    String[] fields() default {};
    
    /**
     * 匹配类型
     */
    MatchType matchType() default MatchType.MIN_MATCH;
    
    /**
     * 处理类型
     */
    HandleType handleType() default HandleType.EXCEPTION;
    
    /**
     * 替换字符（当 handleType 为 REPLACE 时生效）
     */
    char replaceChar() default '*';
    
    /**
     * 自定义异常消息
     */
    String message() default "内容包含敏感词";
}
