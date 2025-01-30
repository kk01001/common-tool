package io.github.kk01001.common.dict.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.github.kk01001.common.dict.DictCache;
import io.github.kk01001.common.dict.annotation.Dict;

import java.io.IOException;

public class DictSerializer extends StdSerializer<String> implements ContextualSerializer {

    private Dict dict;

    public DictSerializer() {
        super(String.class);
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeString(value);
        if (dict != null) {
            String text = DictCache.getDictText(dict.value(), value);
            if (text != null) {
                // 生成字典文本字段
                gen.writeStringField(gen.getOutputContext().getCurrentName() + dict.suffix(), text);
            }
        }
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) {
        if (property != null) {
            Dict annotation = property.getAnnotation(Dict.class);
            if (annotation != null) {
                DictSerializer serializer = new DictSerializer();
                serializer.dict = annotation;
                return serializer;
            }
        }
        return this;
    }
} 