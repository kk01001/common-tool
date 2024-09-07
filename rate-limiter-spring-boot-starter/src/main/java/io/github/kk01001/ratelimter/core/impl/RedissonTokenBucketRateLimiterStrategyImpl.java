package io.github.kk01001.ratelimter.core.impl;

import cn.hutool.core.collection.ListUtil;
import io.github.kk01001.ratelimter.core.RateLimiterStrategy;
import io.github.kk01001.ratelimter.enums.RateLimiterType;
import io.github.kk01001.ratelimter.manager.LuaScriptManager;
import io.github.kk01001.ratelimter.model.Rule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

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
@ConditionalOnClass(RedissonClient.class)
public class RedissonTokenBucketRateLimiterStrategyImpl implements RateLimiterStrategy {

    private final RedissonClient redissonClient;

    @Override
    public RateLimiterType getType() {
        return RateLimiterType.REDISSON_LUA_TOKEN_BUCKET;
    }

    @Override
    public boolean tryAccess(Rule rule) {
        String key = rule.getKey();
        RScript script = redissonClient.getScript();
        String slidingWindowScript = LuaScriptManager.getTokenBucketScript();
        List<Object> keys = ListUtil.of(key);
        Object result = script.eval(RScript.Mode.READ_WRITE,
                slidingWindowScript,
                RScript.ReturnType.VALUE,
                keys,
                rule.getBucketCapacity(),
                rule.getTokenRate(),
                System.currentTimeMillis());

        return Objects.nonNull(result) && 1 == (long) result;
    }

}
