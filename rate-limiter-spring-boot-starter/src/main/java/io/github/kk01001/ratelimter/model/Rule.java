package io.github.kk01001.ratelimter.model;

import io.github.kk01001.ratelimter.enums.RateLimiterType;
import io.github.kk01001.ratelimter.enums.RedisClientType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author linshiqiang
 * @date 2024-09-06 22:09:00
 * @description
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Rule {

    private Boolean enable = true;

    private RateLimiterType rateLimiterType = RateLimiterType.REDIS_LUA_SLIDING_WINDOW;

    private RedisClientType redisClientType = RedisClientType.REDISSON;

    private String key;

    private Integer maxRequests = 50;

    private Integer windowTime = 1;

    /**
     * 是桶的容量（最大令牌数）
     */
    private Integer bucketCapacity = 50;

    /**
     * 是令牌生成速率（每秒生成的令牌数）
     */
    private Integer tokenRate = 10;

    /**
     * 本次申请请求的凭证数
     */
    private Integer permits = 1;
}
