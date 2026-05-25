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
@DesensitizeFor(DesensitizeType.BANK_CARD)
public class BankCardDesensitizeHandler extends AbstractDesensitizeHandler {
    
    @Override
    public String desensitize(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        return value.replaceAll("(\\d{4})\\d+(\\d{4})", "$1****$2");
    }

    @Override
    public String desensitize(String value, Desensitize annotation) {
        return super.desensitize(value, annotation);
    }
} 