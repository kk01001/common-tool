package io.github.kk01001.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

/**
 * @author linshiqiang
 * date:  2024-07-11 13:33
 */
@Slf4j
public class JacksonUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 对象转json
     */
    public static String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("toJson error: ", e);
        }
        return "";
    }

    /**
     * json转对象
     */
    public static <T> T toObject(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("toObject error: ", e);
        }
        return null;
    }

    /**
     * json转对象
     */
    public static <T> T toObject(String json, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            log.error("toObject error: ", e);
        }
        return null;
    }

    /**
     * 对象转对象
     */
    public static <T> T convertValue(Object object, Class<T> clazz) {
        return objectMapper.convertValue(object, clazz);
    }

    /**
     * 对象转对象
     */
    public static <T> T convertValue(Object object, TypeReference<T> toValueTypeRef) {
        return objectMapper.convertValue(object, toValueTypeRef);
    }

}
