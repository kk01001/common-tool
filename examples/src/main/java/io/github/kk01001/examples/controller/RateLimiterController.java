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

    @PostMapping("redisson")
    @RateLimiter(type = RateLimiterType.REDISSON,
            key = "'redissonRateLimiter:'+ #DataModel.code",
            maxRequests = 200,
            windowTime = 5)
    public Boolean redisson(@RequestBody DataModel dataModel) {

        return true;
    }

    @PostMapping("slidingWindow")
    @RateLimiter(type = RateLimiterType.REDISSON_LUA_SLIDING_WINDOW,
            key = "'slidingWindow:'+ #DataModel.code",
            maxRequests = 29,
            windowTime = 1)
    public Boolean slidingWindow(@RequestBody DataModel dataModel) {

        return true;
    }

    @PostMapping("fixedWindow")
    @RateLimiter(type = RateLimiterType.REDISSON_LUA_FIXED_WINDOW,
            key = "'fixedWindow:'+ #DataModel.code",
            maxRequests = 29,
            windowTime = 1)
    public Boolean fixedWindow(@RequestBody DataModel dataModel) {

        return true;
    }

    @PostMapping("tokenBucket")
    @RateLimiter(type = RateLimiterType.REDISSON_LUA_TOKEN_BUCKET,
            key = "'tokenBucket:'+ #DataModel.code",
            bucketCapacity = 29,
            tokenRate = 10)
    public Boolean tokenBucket(@RequestBody DataModel dataModel) {

        return true;
    }

    @PostMapping("leakyBucket")
    @RateLimiter(type = RateLimiterType.REDISSON_LUA_LEAKY_BUCKET,
            key = "'leakyBucket:'+ #DataModel.code",
            bucketCapacity = 3,
            tokenRate = 2,
            permits = 1,
            ruleFunction = "@ruleService.getTokenRate(#dataModel)")
    public Boolean leakyBucket(@RequestBody DataModel dataModel) {

        return true;
    }
}
