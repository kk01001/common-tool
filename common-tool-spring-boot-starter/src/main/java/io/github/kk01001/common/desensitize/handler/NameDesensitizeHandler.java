package io.github.kk01001.common.desensitize.handler;

import io.github.kk01001.common.desensitize.annotation.DesensitizeFor;
import io.github.kk01001.common.desensitize.enums.DesensitizeType;
import org.springframework.util.StringUtils;

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