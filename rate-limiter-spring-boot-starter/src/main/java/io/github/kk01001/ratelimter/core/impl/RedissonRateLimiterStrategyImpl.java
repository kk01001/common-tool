package io.github.kk01001.ratelimter.core.impl;

import io.github.kk01001.ratelimter.core.RateLimiterStrategy;
import io.github.kk01001.ratelimter.enums.RateLimiterType;
import io.github.kk01001.ratelimter.model.FlowRule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Service;

/**
 * @author linshiqiang
 * @date 2024-09-06 22:20:00
 * @description
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnClass(RedissonClient.class)
public class RedissonRateLimiterStrategyImpl implements RateLimiterStrategy {

    private final RedissonClient redissonClient;

    @Override
    public RateLimiterType getType() {
        return RateLimiterType.REDISSON;
    }

    @Override
    public boolean tryAccess(FlowRule flowRule) {
        String key = flowRule.getKey();
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
        // 如果限流器的设置与当前规则不同，则进行设置
        if (isRateLimiterConfigurationChanged(rateLimiter, flowRule)) {
            rateLimiter.setRate(RateType.OVERALL, flowRule.getMaxRequests(), flowRule.getWindowTime(), RateIntervalUnit.SECONDS);
        }
        return rateLimiter.tryAcquire();
    }

    /**
     * 判断限流器的配置是否已改变
     */
    private boolean isRateLimiterConfigurationChanged(RRateLimiter rateLimiter, FlowRule flowRule) {
        // 假设 RRateLimiter 中有方法可以获取当前的速率配置
        RateLimiterConfig config = rateLimiter.getConfig();
        long currentMaxRequests = config.getRate();
        long currentWindowTime = config.getRateInterval();

        return currentMaxRequests != flowRule.getMaxRequests() || currentWindowTime != flowRule.getWindowTime() * 1000;
    }
}
