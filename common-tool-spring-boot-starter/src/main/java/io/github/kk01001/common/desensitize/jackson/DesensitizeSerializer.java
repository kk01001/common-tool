package io.github.kk01001.common.desensitize.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import io.github.kk01001.common.desensitize.annotation.Desensitize;
import io.github.kk01001.common.desensitize.enums.DesensitizeType;
import io.github.kk01001.common.desensitize.handler.DesensitizeHandler;
import io.github.kk01001.common.desensitize.handler.DesensitizeHandlerFactory;

import java.io.IOException;

public class DesensitizeSerializer extends JsonSerializer<String> implements ContextualSerializer {
    
    private final DesensitizeHandlerFactory handlerFactory;
    private DesensitizeHandler handler;
    
    public DesensitizeSerializer(DesensitizeHandlerFactory handlerFactory) {
        this.handlerFactory = handlerFactory;
    }
    
    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (handler != null && value != null) {
            gen.writeString(handler.desensitize(value));
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