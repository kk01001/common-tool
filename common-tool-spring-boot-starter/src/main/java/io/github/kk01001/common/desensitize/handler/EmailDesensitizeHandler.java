package io.github.kk01001.common.desensitize.handler;

import io.github.kk01001.common.desensitize.annotation.DesensitizeFor;
import io.github.kk01001.common.desensitize.enums.DesensitizeType;
import org.springframework.util.StringUtils;

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