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
 * 将时间划分为固定的窗口（比如每分钟、每秒），在每个窗口内，允许的请求次数是固定的。如果在一个窗口内达到限流阈值，则拒绝该窗口内的后续请求。
 * <p>
 * 优点：
 * <p>
 * 实现简单，适合周期性限流的场景。
 * <p>
 * 缺点：
 * <p>
 * 存在“临界问题”，即两个相邻时间窗口切换时，可能在极短时间内接收到比预期更多的请求。
 * <p>
 * 实现思路（Redis版）：
 * <p>
 * 使用 Redis 的计数器（INCR）和过期时间（EXPIRE）来统计每个窗口内的请求数。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisFixedWindowRateLimiterStrategyImpl extends AbstractRedisRateLimiterStrategy implements RateLimiterStrategy {

    @Override
    public RateLimiterType getType() {
        return RateLimiterType.REDIS_LUA_FIXED_WINDOW;
    }

    @Override
    public boolean tryAccess(FlowRule flowRule) {
        return tryAccess(flowRule,
                flowRule.getWindowTime(),
                flowRule.getMaxRequests());
    }

    @Override
    protected String getScript() {
        return LuaScriptManager.getFixedWindowScript();
    }
}
