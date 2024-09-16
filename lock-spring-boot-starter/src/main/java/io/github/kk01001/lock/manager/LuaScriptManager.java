package io.github.kk01001.lock.manager;

import cn.hutool.core.io.resource.ResourceUtil;
import io.github.kk01001.lock.enums.LockType;

import java.util.HashMap;
import java.util.Map;

/**
 * @author linshiqiang
 * @date 2024-09-07 18:22:00
 * @description
 */
public class LuaScriptManager {

    private static final Map<LockType, String> CONFIG_MAP = new HashMap<>(1);

    static {
        CONFIG_MAP.put(LockType.LOCAL, ResourceUtil.readUtf8Str("lua/fixed_window.lua"));
    }
}
