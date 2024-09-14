package io.github.kk01001.ratelimter.core;

import io.github.kk01001.ratelimter.enums.RateLimiterType;
import io.github.kk01001.ratelimter.exception.RateLimitException;
import io.github.kk01001.ratelimter.model.Rule;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author linshiqiang
 * @date 2024-09-06 21:42:00
 * @description
 */
@Component
@RequiredArgsConstructor
public class RateLimiterFactory implements CommandLineRunner {

    private final List<RateLimiterStrategy> strategyList;

    private static final Map<RateLimiterType, RateLimiterStrategy> STRATEGY_MAP = new HashMap<>(8);

    @Override
    public void run(String... args) {
        for (RateLimiterStrategy strategy : strategyList) {
            STRATEGY_MAP.put(strategy.getType(), strategy);
        }
    }

    public boolean tryAccess(Rule rule) {
        if (Objects.isNull(rule)) {
            return true;
        }
        if (!Boolean.TRUE.equals(rule.getEnable())) {
            return true;
        }
        RateLimiterType rateLimiterType = rule.getRateLimiterType();
        return Optional.ofNullable(STRATEGY_MAP.get(rateLimiterType))
                .map(strategy -> strategy.tryAccess(rule))
                .orElseThrow(() -> new RateLimitException("未找到对应的限流策略类型: " + rateLimiterType));
    }
}
