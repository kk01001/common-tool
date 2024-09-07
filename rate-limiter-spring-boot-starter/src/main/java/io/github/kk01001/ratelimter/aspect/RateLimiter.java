package io.github.kk01001.ratelimter.aspect;

import io.github.kk01001.ratelimter.enums.RateLimiterType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author linshiqiang
 * @date 2024-09-06 21:40:00
 * @description
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimiter {

    /**
     * 限流器类型
     */
    RateLimiterType type() default RateLimiterType.LOCAL;

    /**
     * 限流key el
     * ex: key = "'fixedWindow:'+ #DataModel.code",
     */
    String key();

    /**
     * 最大请求数
     */
    int maxRequests() default 10;

    /**
     * 时间窗口，单位为秒
     */
    int windowTime() default 1;

    /**
     * 令牌桶算法:
     * 是桶的容量（最大令牌数）
     */
    int bucketCapacity() default 10;

    /**
     * 令牌桶算法:
     * 是令牌生成速率（每秒生成的令牌数）
     */
    int tokenRate() default 1;

    /**
     * 漏桶算法:
     * 本次申请请求的凭证数
     */
    int permits() default 1;

    /**
     * 限流规则获取方法 el
     */
    String ruleFunction() default "";
}
