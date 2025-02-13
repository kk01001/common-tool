package io.github.kk01001.desensitize.handler;

import io.github.kk01001.desensitize.annotation.DesensitizeFor;
import io.github.kk01001.desensitize.enums.DesensitizeType;
import org.springframework.util.StringUtils;

/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description
 */
@DesensitizeFor(DesensitizeType.EMAIL)
public class EmailDesensitizeHandler implements DesensitizeHandler {

    @Override
    public String desensitize(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        int index = value.indexOf("@");
        if (index <= 1) {
            return value;
        }
        return value.charAt(0) + "****" + value.substring(index);
    }
} 