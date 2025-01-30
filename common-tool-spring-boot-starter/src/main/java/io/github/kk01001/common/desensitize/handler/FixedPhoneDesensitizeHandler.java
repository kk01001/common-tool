package io.github.kk01001.common.desensitize.handler;

import io.github.kk01001.common.desensitize.annotation.DesensitizeFor;
import io.github.kk01001.common.desensitize.enums.DesensitizeType;
import org.springframework.util.StringUtils;

@DesensitizeFor(DesensitizeType.FIXED_PHONE)
public class FixedPhoneDesensitizeHandler implements DesensitizeHandler {
    
    @Override
    public String desensitize(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        // 区号-号码，保留区号和后4位
        int index = value.indexOf("-");
        if (index > 0) {
            String areaCode = value.substring(0, index);
            String number = value.substring(index + 1);
            if (number.length() > 4) {
                return areaCode + "-****" + number.substring(number.length() - 4);
            }
        }
        return value;
    }
} 