package io.github.kk01001.ratelimter.core.impl;

import io.github.kk01001.ratelimter.core.RateLimiterStrategy;
import io.github.kk01001.ratelimter.enums.RateLimiterType;
import io.github.kk01001.ratelimter.manager.LuaScriptManager;
import io.github.kk01001.ratelimter.model.FlowRule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author linshiqiang
 * @date 2024-09-06 22:20:00
 * @description <p>
 * 算法原理：
 * <p>
 * 漏桶算法主要用于控制请求的平滑度。漏桶有固定的容量，并以恒定的速率“漏水”（处理请求）。如果请求过快超过了漏水的速率，则新的请求会被丢弃。
 * <p>
 * 优点：
 * <p>
 * 请求处理平滑，避免突发流量导致的瞬时拥堵。
 * <p>
 * 缺点：
 * <p>
 * 不允许任何突发流量，所有请求都以恒定速率处理。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisLeakyBucketRateLimiterStrategyImpl extends AbstractRedisRateLimiterStrategy implements RateLimiterStrategy {

    @Override
    public RateLimiterType getType() {
        return RateLimiterType.REDIS_LUA_LEAKY_BUCKET;
    }

    @Override
    public boolean tryAccess(FlowRule flowRule) {
        return tryAccess(flowRule,
                flowRule.getBucketCapacity(),
                flowRule.getTokenRate(),
                System.currentTimeMillis(),
                flowRule.getPermits());
    }

    @Override
    public String getScript() {
        return LuaScriptManager.getLeakyBucketScript();
    }
}
