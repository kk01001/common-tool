package io.github.kk01001.ratelimter.core.impl;

import cn.hutool.core.util.IdUtil;
import io.github.kk01001.ratelimter.core.RateLimiterStrategy;
import io.github.kk01001.ratelimter.enums.RateLimiterType;
import io.github.kk01001.ratelimter.manager.LuaScriptManager;
import io.github.kk01001.ratelimter.model.Rule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Service;

/**
 * @author linshiqiang
 * @date 2024-09-06 22:20:00
 * @description <p>
 * 算法原理：
 * <p>
 * 滑动窗口比固定时间窗口更加精确。它维护一个记录时间戳的队列，每次请求时都根据时间戳计算窗口内的请求数，并移除超出时间窗口的旧请求。
 * <p>
 * 优点：
 * <p>
 * 精确度高，避免了固定窗口算法中的“临界问题”。
 * <p>
 * 缺点：
 * <p>
 * 实现复杂度较高，相对资源占用更多。
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnClass(RedissonClient.class)
public class RedisSlidingWindowRateLimiterStrategyImpl extends AbstractRedisRateLimiterStrategy implements RateLimiterStrategy {

    @Override
    public RateLimiterType getType() {
        return RateLimiterType.REDIS_LUA_SLIDING_WINDOW;
    }

    @Override
    public boolean tryAccess(Rule rule) {
        return tryAccess(rule,
                rule.getWindowTime(),
                rule.getMaxRequests(),
                System.currentTimeMillis(),
                IdUtil.fastSimpleUUID());
    }

    @Override
    protected String getScript() {
        return LuaScriptManager.getSlidingWindowScript();
    }
}
