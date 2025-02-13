package io.github.kk01001.desensitize.handler;

import io.github.kk01001.desensitize.annotation.DesensitizeFor;
import io.github.kk01001.desensitize.enums.DesensitizeType;
import org.springframework.util.StringUtils;

/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description
 */
@DesensitizeFor(DesensitizeType.NAME)
public class NameDesensitizeHandler implements DesensitizeHandler {

    @Override
    public String desensitize(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        int length = value.length();
        if (length <= 1) {
            return value;
        } else if (length == 2) {
            return value.charAt(0) + "*";
        } else {
            return value.charAt(0) + "*".repeat(length - 2) + value.substring(length - 1);
        }
    }
} 