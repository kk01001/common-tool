package io.github.kk01001.common.desensitize.handler;

import io.github.kk01001.common.desensitize.annotation.DesensitizeFor;
import io.github.kk01001.common.desensitize.enums.DesensitizeType;
import org.springframework.util.StringUtils;

@DesensitizeFor(DesensitizeType.CAR_NUMBER)
public class CarNumberDesensitizeHandler implements DesensitizeHandler {
    
    @Override
    public String desensitize(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        // 保留省份简称和最后一位
        return value.substring(0, 2) + "****" + value.charAt(value.length() - 1);
    }
} 