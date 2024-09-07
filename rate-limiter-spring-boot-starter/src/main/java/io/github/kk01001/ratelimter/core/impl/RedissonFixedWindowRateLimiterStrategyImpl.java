package io.github.kk01001.ratelimter.core.impl;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.IdUtil;
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
@ConditionalOnClass(RedissonClient.class)
public class RedissonFixedWindowRateLimiterStrategyImpl implements RateLimiterStrategy {

    private final RedissonClient redissonClient;

    @Override
    public RateLimiterType getType() {
        return RateLimiterType.REDISSON_LUA_FIXED_WINDOW;
    }

    @Override
    public boolean tryAccess(Rule rule) {
        String key = rule.getKey();
        RScript script = redissonClient.getScript();
        String slidingWindowScript = LuaScriptManager.getFixedWindowScript();
        List<Object> keys = ListUtil.of(key);
        Object result = script.eval(RScript.Mode.READ_WRITE,
                slidingWindowScript,
                RScript.ReturnType.VALUE,
                keys,
                rule.getWindowTime(),
                rule.getMaxRequests(),
                System.currentTimeMillis(),
                IdUtil.fastSimpleUUID());

        return Objects.nonNull(result) && 1 == (long) result;
    }

}
