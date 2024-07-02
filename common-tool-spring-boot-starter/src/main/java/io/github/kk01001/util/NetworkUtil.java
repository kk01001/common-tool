package io.github.kk01001.util;

import cn.hutool.core.net.NetUtil;
import org.springframework.util.StringUtils;

/**
 * @author kk01001
 * date:  2023-08-07 17:28
 */
public class NetworkUtil {

    private static final String SERVER_IP = "SERVER_IP";

    public static String getLocalIp() {
        String serverIP = getSystemProperty(SERVER_IP);
        if (StringUtils.hasLength(serverIP)) {
            return serverIP;
        }
        return NetUtil.getLocalhostStr();
    }

    /**
     * System environment -> System properties
     *
     * @param key key
     * @return value
     */
    public static String getSystemProperty(String key) {
        String value = System.getenv(key);
        if (StringUtils.hasLength(value)) {
            return value;
        }
        return System.getProperty(key);
    }
}
