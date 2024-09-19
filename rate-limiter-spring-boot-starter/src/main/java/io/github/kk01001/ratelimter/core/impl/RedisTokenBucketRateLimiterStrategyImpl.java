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
 * 令牌桶算法允许请求以平稳的速度通过，但也能允许一定程度的突发请求。系统维护一个容量固定的“令牌桶”，桶里有一些令牌，按一定速率往桶里添加令牌。
 * 每次有请求进来时，消耗一个令牌，若无令牌可用，则限流。
 * <p>
 * 优点：
 * <p>
 * 适合突发请求的场景。
 * 能平滑地限制请求速率，支持一定的突发流量。
 * <p>
 * 缺点：
 * <p>
 * 实现稍复杂，特别是在高并发下对性能要求较高。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisTokenBucketRateLimiterStrategyImpl extends AbstractRedisRateLimiterStrategy implements RateLimiterStrategy {

    @Override
    public RateLimiterType getType() {
        return RateLimiterType.REDIS_LUA_TOKEN_BUCKET;
    }

    @Override
    public boolean tryAccess(FlowRule flowRule) {

        return tryAccess(flowRule,
                flowRule.getBucketCapacity(),
                flowRule.getTokenRate(),
                System.currentTimeMillis());
    }

    @Override
    public String getScript() {
        return LuaScriptManager.getTokenBucketScript();
    }

}
