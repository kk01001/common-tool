package io.github.kk01001.ratelimter.core.impl;

import com.google.common.util.concurrent.RateLimiter;
import io.github.kk01001.ratelimter.core.RateLimiterStrategy;
import io.github.kk01001.ratelimter.enums.RateLimiterType;
import io.github.kk01001.ratelimter.model.Rule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author linshiqiang
 * @date 2024-09-06 22:20:00
 * @description
 */
@Slf4j
@SuppressWarnings("all")
@Service
@RequiredArgsConstructor
@ConditionalOnClass(RateLimiter.class)
public class GuavaRateLimiterStrategyImpl implements RateLimiterStrategy {

    private static final Map<String, RateLimiter> rateLimiterMap = new ConcurrentHashMap<>();

    @Override
    public RateLimiterType getType() {
        return RateLimiterType.GUAVA;
    }

    @Override
    public boolean tryAccess(Rule rule) {
        String key = rule.getKey();
        RateLimiter rateLimiter = getRateLimiter(key, rule.getTokenRate(), rule.getWindowTime(), TimeUnit.SECONDS);
        return rateLimiter.tryAcquire();
    }

    private RateLimiter getRateLimiter(String key, double permitsPerSecond, long warmupPeriod, TimeUnit unit) {
        return rateLimiterMap.computeIfAbsent(key, k -> RateLimiter.create(permitsPerSecond, warmupPeriod, unit));
    }
}
