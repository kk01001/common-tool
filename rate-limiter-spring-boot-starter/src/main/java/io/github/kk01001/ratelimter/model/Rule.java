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

    private RateLimiterType rateLimiterType;

    private RedisClientType redisClientType;

    private String key;

    private Integer maxRequests;

    private Integer windowTime;

    /**
     * 是桶的容量（最大令牌数）
     */
    private Integer bucketCapacity;

    /**
     * 是令牌生成速率（每秒生成的令牌数）
     */
    private Integer tokenRate;

    /**
     * 本次申请请求的凭证数
     */
    private Integer permits;
}
