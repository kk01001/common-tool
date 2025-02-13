package io.github.kk01001.desensitize.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.github.kk01001.desensitize.handler.DesensitizeHandlerFactory;
import io.github.kk01001.desensitize.jackson.DesensitizeSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 脱敏工具类
 * 
 * @author kk01001
 * @date 2025-02-13 14:31:00
 */
@Slf4j
@Component
public class DesensitizeUtil implements ApplicationContextAware {

    private static ObjectMapper objectMapper;
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        // 初始化ObjectMapper并配置脱敏序列化器
        objectMapper = new ObjectMapper();
        DesensitizeHandlerFactory handlerFactory = applicationContext.getBean(DesensitizeHandlerFactory.class);
        SimpleModule module = new SimpleModule();
        module.addSerializer(String.class, new DesensitizeSerializer(handlerFactory));
        objectMapper.registerModule(module);
    }

    /**
     * 对象脱敏并转换为JSON字符串
     *
     * @param object 需要脱敏的对象
     * @return 脱敏后的JSON字符串
     */
    public static String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("Object desensitize to json error:", e);
            return null;
        }
    }

    /**
     * JSON字符串转换为脱敏对象
     *
     * @param json JSON字符串
     * @param clazz 目标对象类型
     * @return 脱敏后的对象
     */
    public static <T> T toObject(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("Json to desensitized object error:", e);
            return null;
        }
    }

    /**
     * JSON字符串转换为脱敏对象列表
     *
     * @param json JSON字符串
     * @param typeReference 目标类型引用
     * @return 脱敏后的对象列表
     */
    public static <T> T toObject(String json, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            log.error("Json to desensitized object list error:", e);
            return null;
        }
    }

    /**
     * 对象转换为脱敏对象
     *
     * @param object 源对象
     * @param clazz 目标对象类型
     * @return 脱敏后的对象
     */
    public static <T> T convertObject(Object object, Class<T> clazz) {
        return objectMapper.convertValue(object, clazz);
    }

    /**
     * 对象转换为脱敏对象列表
     *
     * @param object 源对象
     * @param typeReference 目标类型引用
     * @return 脱敏后的对象列表
     */
    public static <T> T convertObject(Object object, TypeReference<T> typeReference) {
        return objectMapper.convertValue(object, typeReference);
    }
} 