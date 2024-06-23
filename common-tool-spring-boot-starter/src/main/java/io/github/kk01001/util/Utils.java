package io.github.kk01001.util;

import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;

/**
 * @author linshiqiang
 * date 2024-06-21 21:07:00
 */
public class Utils {

    private static final String SERVER_IP_KEY = "SERVER_IP";

    public static String getLocalServerIp() {
        String serverIp = SpringUtil.getProperty(SERVER_IP_KEY);
        if (StrUtil.isNotEmpty(serverIp)) {
            return serverIp;
        }
        return NetUtil.getLocalhostStr();
    }
}
