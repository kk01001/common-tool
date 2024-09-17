package io.github.kk01001.lock.manager;

import cn.hutool.core.io.resource.ResourceUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @author linshiqiang
 * @date 2024-09-07 18:22:00
 * @description
 */
public class LuaScriptManager {

    private static final Map<String, String> CONFIG_MAP = new HashMap<>(1);

    static {
        CONFIG_MAP.put("semaphore-try-acquire", ResourceUtil.readUtf8Str("lua/semaphore-try-acquire.lua"));
        CONFIG_MAP.put("semaphore-release", ResourceUtil.readUtf8Str("lua/semaphore-release.lua"));
    }

    public static String getSemaphoreTryAcquire() {
        return CONFIG_MAP.get("semaphore-try-acquire");
    }

    public static String getSemaphoreRelease() {
        return CONFIG_MAP.get("semaphore-release");
    }

}
