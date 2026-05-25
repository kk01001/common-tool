package io.github.archer099.desensitize.desensitize.handler;

import io.github.archer099.desensitize.desensitize.annotation.Desensitize;
import io.github.archer099.desensitize.desensitize.annotation.DesensitizeFor;
import io.github.archer099.desensitize.desensitize.enums.DesensitizeType;
import org.springframework.util.StringUtils;

/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description
 */
@DesensitizeFor(DesensitizeType.CAR_NUMBER)
public class CarNumberDesensitizeHandler extends AbstractDesensitizeHandler {
    
    @Override
    public String desensitize(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        // 保留省份简称和最后一位
        return value.substring(0, 2) + "****" + value.charAt(value.length() - 1);
    }

    @Override
    public String desensitize(String value, Desensitize annotation) {
        return super.desensitize(value, annotation);
    }
} 