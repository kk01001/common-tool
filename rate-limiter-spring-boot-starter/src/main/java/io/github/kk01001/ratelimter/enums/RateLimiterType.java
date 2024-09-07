package io.github.kk01001.ratelimter.enums;

/**
 * @author linshiqiang
 * @date 2024-09-06 21:38:00
 * @description
 */
public enum RateLimiterType {

    LOCAL,

    /**
     * redisson自带RRateLimiter
     */
    REDISSON,

    /**
     * redisson lua 滑动窗口算法
     */
    REDISSON_LUA_SLIDING_WINDOW,

    /**
     * redisson lua 固定窗口算法
     */
    REDISSON_LUA_FIXED_WINDOW,

    /**
     * redisson lua 令牌桶算法
     */
    REDISSON_LUA_TOKEN_BUCKET,

    /**
     * redisson lua 漏桶算法
     */
    REDISSON_LUA_LEAKY_BUCKET,
}
