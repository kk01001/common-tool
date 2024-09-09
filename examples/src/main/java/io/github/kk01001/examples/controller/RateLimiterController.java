package io.github.kk01001.examples.controller;

import io.github.kk01001.examples.model.DataModel;
import io.github.kk01001.ratelimter.aspect.RateLimiter;
import io.github.kk01001.ratelimter.enums.RateLimiterType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author linshiqiang
 * @date 2024-09-07 11:17:00
 * @description
 */
@RestController
@RequestMapping("rate-limiter")
public class RateLimiterController {

    @PostMapping("guava")
    @RateLimiter(type = RateLimiterType.GUAVA,
            key = "'guava:'+ #dataModel.code",
            tokenRate = 20,
            windowTime = 5)
    public Boolean guava(@RequestBody DataModel dataModel) {

        return true;
    }

    @PostMapping("redisson")
    @RateLimiter(type = RateLimiterType.REDISSON,
            key = "'redissonRateLimiter:'+ #dataModel.code",
            maxRequests = 200,
            windowTime = 5)
    public Boolean redisson(@RequestBody DataModel dataModel) {

        return true;
    }

    @PostMapping("slidingWindow")
    @RateLimiter(type = RateLimiterType.REDIS_LUA_SLIDING_WINDOW,
            key = "'slidingWindow:'+ #dataModel.code",
            maxRequests = 29,
            windowTime = 1)
    public Boolean slidingWindow(@RequestBody DataModel dataModel) {

        return true;
    }

    @PostMapping("fixedWindow")
    @RateLimiter(type = RateLimiterType.REDIS_LUA_FIXED_WINDOW,
            key = "'fixedWindow:'+ #dataModel.code",
            maxRequests = 29,
            windowTime = 1)
    public Boolean fixedWindow(@RequestBody DataModel dataModel) {

        return true;
    }

    @PostMapping("tokenBucket")
    @RateLimiter(type = RateLimiterType.REDIS_LUA_TOKEN_BUCKET,
            key = "'tokenBucket:'+ #dataModel.code",
            bucketCapacity = 29,
            tokenRate = 10)
    public Boolean tokenBucket(@RequestBody DataModel dataModel) {

        return true;
    }

    @PostMapping("leakyBucket")
    @RateLimiter(type = RateLimiterType.REDIS_LUA_LEAKY_BUCKET,
            // key = "'leakyBucket:'+ #DataModel.code",
            key = "@ruleService.getKey(#dataModel)",
            ruleFunction = "@ruleService.getRule(#dataModel)",
            bucketCapacity = 3,
            bucketCapacityFunction = "@ruleService.getBucketCapacity(#dataModel)",
            tokenRate = 2,
            tokenRateFunction = "@ruleService.getTokenRate(#dataModel)",
            permits = 1,
            maxRequestsFunction = "@ruleService.getMaxRequests(#dataModel)")
    public Boolean leakyBucket(@RequestBody DataModel dataModel) {

        return true;
    }
}
