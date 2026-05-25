package io.github.archer099.sensitive.annotation;

import io.github.archer099.sensitive.core.HandleType;
import io.github.archer099.sensitive.core.MatchType;

import java.lang.annotation.*;

/**
 * @author archer099
 * @date 2026-01-18 13:02:31
 * @description 敏感词字段注解，用于标记需要进行敏感词过滤的字段
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SensitiveWordField {
    
    /**
     * 匹配类型
     */
    MatchType matchType() default MatchType.MIN_MATCH;
    
    /**
     * 处理类型
     */
    HandleType handleType() default HandleType.REPLACE;
    
    /**
     * 替换字符（当 handleType 为 REPLACE 时生效）
     */
    char replaceChar() default '*';
}
