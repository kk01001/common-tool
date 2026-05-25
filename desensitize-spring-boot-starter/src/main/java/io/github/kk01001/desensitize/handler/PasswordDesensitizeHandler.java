package io.github.archer099.desensitize.handler;

import io.github.archer099.desensitize.annotation.Desensitize;
import io.github.archer099.desensitize.annotation.DesensitizeFor;
import io.github.archer099.desensitize.enums.DesensitizeType;
import org.springframework.util.StringUtils;

/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description
 */
@DesensitizeFor(DesensitizeType.PASSWORD)
public class PasswordDesensitizeHandler extends AbstractDesensitizeHandler {
    
    @Override
    public String desensitize(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        return "******";
    }

    @Override
    public String desensitize(String value, Desensitize annotation) {
        return super.desensitize(value, annotation);
    }
} 