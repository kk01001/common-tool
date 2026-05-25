package io.github.archer099.ratelimter.core;

import io.github.archer099.ratelimter.enums.RateLimiterType;
import io.github.archer099.ratelimter.model.FlowRule;

/**
 * @author linshiqiang
 * @date 2024-09-06 21:39:00
 * @description
 */
public interface RateLimiterStrategy {

    RateLimiterType getType();

    boolean tryAccess(FlowRule flowRule);
}
