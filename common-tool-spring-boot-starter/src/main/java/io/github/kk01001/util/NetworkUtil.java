package io.github.kk01001.util;

import cn.hutool.core.net.NetUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

/**
 * @author kk01001
 * date:  2023-08-07 17:28
 */
public class NetworkUtil {

    public static final String LOCAL_SERVER_IP = getLocalIp();

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

    public static String getClientIp(HttpServletRequest request) {
        // 优先尝试获取 X-Forwarded-For 头中的信息
        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            // 如果 X-Forwarded-For 为空，尝试获取 X-Real-IP 头中的信息
            ip = request.getHeader("X-Real-IP");
        }

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            // 如果 X-Real-IP 也为空，最终回退到 getRemoteAddr()
            ip = request.getRemoteAddr();
        }

        // 如果 X-Forwarded-For 中有多个 IP 地址，返回第一个地址
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0];
        }
        return ip;
    }
}
