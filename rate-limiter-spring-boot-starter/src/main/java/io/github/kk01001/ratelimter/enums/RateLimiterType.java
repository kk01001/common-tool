package io.github.kk01001.ratelimter.enums;

import org.redisson.api.RRateLimiter;

/**
 * @author linshiqiang
 * @date 2024-09-06 21:38:00
 * @description
 */
public enum RateLimiterType {

    LOCAL,

    GUAVA,

    /**
     * redisson
     * @see RRateLimiter
     */
    REDISSON,

    /**
     * redis lua 固定窗口算法
     */
    REDIS_LUA_FIXED_WINDOW,

    /**
     * redis lua 滑动窗口算法
     */
    REDIS_LUA_SLIDING_WINDOW,

    /**
     * redis lua 令牌桶算法
     */
    REDIS_LUA_TOKEN_BUCKET,

    /**
     * redis lua 漏桶算法
     */
    REDIS_LUA_LEAKY_BUCKET,

}
