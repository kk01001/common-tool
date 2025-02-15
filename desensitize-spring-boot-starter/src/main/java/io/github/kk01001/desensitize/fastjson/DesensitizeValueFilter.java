package io.github.kk01001.desensitize.fastjson;

import com.alibaba.fastjson.serializer.ValueFilter;
import io.github.kk01001.desensitize.annotation.Desensitize;
import io.github.kk01001.desensitize.enums.DesensitizeType;
import io.github.kk01001.desensitize.handler.DesensitizeHandler;
import io.github.kk01001.desensitize.handler.DesensitizeHandlerFactory;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Field;

/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description
 */
@RequiredArgsConstructor
public class DesensitizeValueFilter implements ValueFilter {
    
    private final DesensitizeHandlerFactory handlerFactory;
    
    @Override
    public Object process(Object object, String name, Object value) {
        if (value instanceof String && object != null) {
            try {
                Field field = object.getClass().getDeclaredField(name);
                Desensitize annotation = field.getAnnotation(Desensitize.class);
                if (annotation != null) {
                    DesensitizeHandler handler;
                    if (annotation.type() == DesensitizeType.CUSTOM) {
                        handler = annotation.handler().getDeclaredConstructor().newInstance();
                    } else {
                        handler = handlerFactory.getHandler(annotation.type());
                    }
                    if (handler != null) {
                        return handler.desensitize((String) value, annotation);
                    }
                }
            } catch (Exception ignored) {
                // 如果获取字段或处理失败，返回原值
            }
        }
        return value;
    }
} 