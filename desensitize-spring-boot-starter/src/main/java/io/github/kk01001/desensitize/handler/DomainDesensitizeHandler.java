package io.github.kk01001.desensitize.handler;

import io.github.kk01001.desensitize.annotation.Desensitize;
import io.github.kk01001.desensitize.annotation.DesensitizeFor;
import io.github.kk01001.desensitize.enums.DesensitizeType;
import org.springframework.util.StringUtils;

/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description 域名脱敏处理器
 */
@DesensitizeFor(DesensitizeType.DOMAIN)
public class DomainDesensitizeHandler extends AbstractDesensitizeHandler {

    @Override
    public String desensitize(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }

        // 处理带端口号的情况
        String domain = value;
        String port = "";

        int portIndex = value.lastIndexOf(":");
        if (portIndex > 0) {
            domain = value.substring(0, portIndex);
            port = value.substring(portIndex);
        }

        // 检查是否是IP地址格式
        if (isIpAddress(domain)) {
            String[] parts = domain.split("\\.");
            if (parts.length == 4) {
                return parts[0] + ".*.*." + parts[3] + port;
            }
            return value;
        }

        // 处理域名格式
        String[] parts = domain.split("\\.");
        if (parts.length <= 2) {
            return value; // 如果域名部分少于等于2段，返回原值
        }

        // 处理子域名部分
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i == 0) {
                // 第一段用星号代替
                result.append("****");
            } else {
                // 其他段保持不变
                result.append(".").append(parts[i]);
            }
        }

        // 添加端口号（如果有）
        return result.append(port).toString();
    }

    /**
     * 判断是否为IP地址格式
     */
    private boolean isIpAddress(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        String[] parts = value.split("\\.");
        if (parts.length != 4) {
            return false;
        }
        try {
            for (String part : parts) {
                int num = Integer.parseInt(part);
                if (num < 0 || num > 255) {
                    return false;
                }
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public String desensitize(String value, Desensitize annotation) {
        return super.desensitize(value, annotation);
    }
}