package io.github.kk01001.ratelimter.manager;

import cn.hutool.core.io.resource.ResourceUtil;
import io.github.kk01001.ratelimter.enums.RateLimiterType;

import java.util.HashMap;
import java.util.Map;

/**
 * @author linshiqiang
 * @date 2024-09-07 18:22:00
 * @description
 */
public class LuaScriptManager {

    private static final Map<RateLimiterType, String> CONFIG_MAP = new HashMap<>(1);

    static {
        CONFIG_MAP.put(RateLimiterType.REDIS_LUA_FIXED_WINDOW, ResourceUtil.readUtf8Str("lua/fixed_window.lua"));
        CONFIG_MAP.put(RateLimiterType.REDIS_LUA_SLIDING_WINDOW, ResourceUtil.readUtf8Str("lua/sliding_window.lua"));
        CONFIG_MAP.put(RateLimiterType.REDIS_LUA_TOKEN_BUCKET, ResourceUtil.readUtf8Str("lua/token_bucket.lua"));
        CONFIG_MAP.put(RateLimiterType.REDIS_LUA_LEAKY_BUCKET, ResourceUtil.readUtf8Str("lua/leaky_bucket.lua"));
    }

    public static String getFixedWindowScript() {

        return CONFIG_MAP.get(RateLimiterType.REDIS_LUA_FIXED_WINDOW);
    }

    public static String getSlidingWindowScript() {

        return CONFIG_MAP.get(RateLimiterType.REDIS_LUA_SLIDING_WINDOW);
    }

    public static String getTokenBucketScript() {

        return CONFIG_MAP.get(RateLimiterType.REDIS_LUA_TOKEN_BUCKET);
    }

    public static String getLeakyBucketScript() {

        return CONFIG_MAP.get(RateLimiterType.REDIS_LUA_LEAKY_BUCKET);
    }
}
