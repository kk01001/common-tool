package io.github.kk01001.desensitize.handler;

import io.github.kk01001.desensitize.annotation.Desensitize;
import io.github.kk01001.desensitize.annotation.DesensitizeFor;
import io.github.kk01001.desensitize.enums.DesensitizeType;
import org.springframework.util.StringUtils;

/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description
 */
@DesensitizeFor(DesensitizeType.ADDRESS)
public class AddressDesensitizeHandler extends AbstractDesensitizeHandler {
    
    @Override
    public String desensitize(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        int length = value.length();
        if (length <= 8) {
            return value;
        }
        // 保留前6位和后2位
        return value.substring(0, 6) + "****" + value.substring(length - 2);
    }

    @Override
    public String desensitize(String value, Desensitize annotation) {
        return super.desensitize(value, annotation);
    }
} 