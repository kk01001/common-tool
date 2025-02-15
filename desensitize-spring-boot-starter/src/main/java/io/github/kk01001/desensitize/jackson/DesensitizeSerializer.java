package io.github.kk01001.desensitize.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import io.github.kk01001.desensitize.annotation.Desensitize;
import io.github.kk01001.desensitize.enums.DesensitizeType;
import io.github.kk01001.desensitize.handler.DesensitizeHandler;
import io.github.kk01001.desensitize.handler.DesensitizeHandlerFactory;

import java.io.IOException;

/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description
 */
public class DesensitizeSerializer extends JsonSerializer<String> implements ContextualSerializer {
    
    private final DesensitizeHandlerFactory handlerFactory;
    private DesensitizeHandler handler;
    private Desensitize desensitize;
    
    public DesensitizeSerializer(DesensitizeHandlerFactory handlerFactory) {
        this.handlerFactory = handlerFactory;
    }
    
    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (handler != null && value != null) {
            gen.writeString(handler.desensitize(value, desensitize));
        } else {
            gen.writeString(value);
        }
    }
    
    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) {
        if (property != null) {
            Desensitize annotation = property.getAnnotation(Desensitize.class);
            if (annotation != null) {
                DesensitizeSerializer serializer = new DesensitizeSerializer(handlerFactory);
                serializer.desensitize = annotation;
                if (annotation.type() == DesensitizeType.CUSTOM) {
                    try {
                        serializer.handler = annotation.handler().getDeclaredConstructor().newInstance();
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to create custom desensitize handler", e);
                    }
                } else {
                    serializer.handler = handlerFactory.getHandler(annotation.type());
                }
                return serializer;
            }
        }
        return this;
    }
} 