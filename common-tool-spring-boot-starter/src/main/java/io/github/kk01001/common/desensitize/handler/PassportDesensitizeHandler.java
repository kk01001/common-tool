package io.github.kk01001.common.desensitize.handler;

import io.github.kk01001.common.desensitize.annotation.DesensitizeFor;
import io.github.kk01001.common.desensitize.enums.DesensitizeType;
import org.springframework.util.StringUtils;

@DesensitizeFor(DesensitizeType.PASSPORT)
public class PassportDesensitizeHandler implements DesensitizeHandler {
    
    @Override
    public String desensitize(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        // 护照号一般是1-2个字母+6-7个数字，保留前2位和后2位
        if (value.length() > 4) {
            return value.substring(0, 2) + "****" + value.substring(value.length() - 2);
        }
        return value;
    }
} 