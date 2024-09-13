package io.github.kk01001.ratelimter.aspect;

import io.github.kk01001.ratelimter.enums.RateLimiterType;
import io.github.kk01001.ratelimter.enums.RedisClientType;

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
     * redis客户端类型
     */
    RedisClientType redisClientType() default RedisClientType.REDISSON;

    /**
     * 是否开启限流
     */
    boolean enable() default true;

    /**
     * 限流key el
     * ex: key = "'fixedWindow:'+ #DataModel.code",
     */
    String key() default "defaultRateLimiterKey";

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
     * 最大请求数 el
     */
    String maxRequestsFunction() default "";

    String windowTimeFunction() default "";

    String bucketCapacityFunction() default "";

    String tokenRateFunction() default "";

    String permitsFunction() default "";

    /**
     * 自定义规则查询方法
     * 优先级最高
     */
    String ruleFunction() default "";
}
