package io.github.kk01001.desensitize.handler;

import org.springframework.util.StringUtils;

import io.github.kk01001.desensitize.annotation.Desensitize;
import io.github.kk01001.desensitize.annotation.DesensitizeFor;
import io.github.kk01001.desensitize.enums.DesensitizeType;

/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description
 */
@DesensitizeFor(DesensitizeType.IPV4)
public class IpDesensitizeHandler extends AbstractDesensitizeHandler {
    
    @Override
    public String desensitize(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }

        // 处理带端口号的情况
        String ip = value;
        String port = "";
        
        int portIndex = value.lastIndexOf(":");
        if (portIndex > 0) {
            ip = value.substring(0, portIndex);
            port = value.substring(portIndex);
        }

        String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            return value;
        }

        // 保留第一段和最后一段，中间两段用*替代
        return parts[0] + ".*.*." + parts[3] + port;
    }

    @Override
    public String desensitize(String value, Desensitize annotation) {
        return super.desensitize(value, annotation);
    }
} 