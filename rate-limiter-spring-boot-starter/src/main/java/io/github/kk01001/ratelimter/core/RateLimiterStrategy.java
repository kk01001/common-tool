package io.github.kk01001.ratelimter.core;

import io.github.kk01001.ratelimter.enums.RateLimiterType;
import io.github.kk01001.ratelimter.model.Rule;

/**
 * @author linshiqiang
 * @date 2024-09-06 21:39:00
 * @description
 */
public interface RateLimiterStrategy {

    RateLimiterType getType();

    boolean tryAccess(Rule rule);
}
