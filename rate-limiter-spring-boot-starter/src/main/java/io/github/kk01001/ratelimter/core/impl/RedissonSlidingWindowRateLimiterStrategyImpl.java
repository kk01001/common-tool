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
public class RedissonSlidingWindowRateLimiterStrategyImpl implements RateLimiterStrategy {

    private final RedissonClient redissonClient;

    @Override
    public RateLimiterType getType() {
        return RateLimiterType.REDISSON_LUA_SLIDING_WINDOW;
    }

    @Override
    public boolean tryAccess(Rule rule) {
        String key = rule.getKey();
        RScript script = redissonClient.getScript();
        String slidingWindowScript = LuaScriptManager.getSlidingWindowScript();
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
