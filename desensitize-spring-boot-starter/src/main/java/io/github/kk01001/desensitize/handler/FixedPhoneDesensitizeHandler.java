package io.github.kk01001.desensitize.handler;

import io.github.kk01001.desensitize.annotation.DesensitizeFor;
import io.github.kk01001.desensitize.enums.DesensitizeType;
import org.springframework.util.StringUtils;

/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description
 */
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