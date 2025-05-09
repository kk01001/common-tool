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
@DesensitizeFor(DesensitizeType.ID_CARD)
public class IdCardDesensitizeHandler extends AbstractDesensitizeHandler {
    
    @Override
    public String desensitize(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        return value.replaceAll("(\\d{4})\\d{10}(\\w{4})", "$1**********$2");
    }

    @Override
    public String desensitize(String value, Desensitize annotation) {
        return super.desensitize(value, annotation);
    }
} 