package io.github.kk01001.sensitive.handler;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import io.github.kk01001.sensitive.annotation.SensitiveWordCheck;
import io.github.kk01001.sensitive.annotation.SensitiveWordField;
import io.github.kk01001.sensitive.core.*;
import io.github.kk01001.sensitive.properties.SensitiveWordProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;

/**
 * @author kk01001
 * @date 2026-01-18 13:02:31
 * @description 敏感词检测 AOP 切面
 */
@Slf4j
@Aspect
@RequiredArgsConstructor
public class SensitiveWordAspect {
    
    private final SensitiveWordFilter sensitiveWordFilter;
    private final SensitiveWordProperties properties;
    
    @Around("@annotation(io.github.kk01001.sensitive.annotation.SensitiveWordCheck)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        SensitiveWordCheck annotation = method.getAnnotation(SensitiveWordCheck.class);
        
        Object[] args = joinPoint.getArgs();
        Parameter[] parameters = method.getParameters();
        
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg == null) {
                continue;
            }
            
            // 检查参数是否有 @SensitiveWordCheck 注解
            SensitiveWordCheck paramAnnotation = parameters[i].getAnnotation(SensitiveWordCheck.class);
            SensitiveWordCheck effectiveAnnotation = paramAnnotation != null ? paramAnnotation : annotation;
            
            if (arg instanceof String text) {
                args[i] = processString(text, effectiveAnnotation);
            } else {
                processObject(arg, effectiveAnnotation);
            }
        }
        
        return joinPoint.proceed(args);
    }
    
    /**
     * 处理字符串
     */
    private String processString(String text, SensitiveWordCheck annotation) {
        if (StrUtil.isBlank(text)) {
            return text;
        }
        
        List<SensitiveWordResult> results = sensitiveWordFilter.findAll(text, annotation.matchType());
        
        if (results.isEmpty()) {
            return text;
        }
        
        return switch (annotation.handleType()) {
            case EXCEPTION -> {
                String message = StrUtil.format(annotation.message() + ": {}", 
                        results.stream().map(SensitiveWordResult::getWord).toList());
                throw new SensitiveWordException(message, results);
            }
            case REPLACE -> sensitiveWordFilter.replace(text, annotation.replaceChar(), annotation.matchType());
            case HIGHLIGHT -> sensitiveWordFilter.highlight(text, 
                    properties.getHighlightStartTag(), 
                    properties.getHighlightEndTag(), 
                    annotation.matchType());
            case DETECT_ONLY -> {
                log.warn("检测到敏感词: {}", results.stream().map(SensitiveWordResult::getWord).toList());
                yield text;
            }
        };
    }
    
    /**
     * 处理对象
     */
    private void processObject(Object obj, SensitiveWordCheck annotation) {
        if (obj == null) {
            return;
        }
        
        String[] fields = annotation.fields();
        Field[] allFields = ReflectUtil.getFields(obj.getClass());
        
        for (Field field : allFields) {
            if (!String.class.equals(field.getType())) {
                continue;
            }
            
            // 检查是否在指定字段列表中
            if (fields.length > 0) {
                boolean found = false;
                for (String f : fields) {
                    if (f.equals(field.getName())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    continue;
                }
            }
            
            // 检查字段是否有 @SensitiveWordField 注解
            SensitiveWordField fieldAnnotation = field.getAnnotation(SensitiveWordField.class);
            
            Object value = ReflectUtil.getFieldValue(obj, field);
            if (value instanceof String text && StrUtil.isNotBlank(text)) {
                MatchType matchType = fieldAnnotation != null ? fieldAnnotation.matchType() : annotation.matchType();
                HandleType handleType = fieldAnnotation != null ? fieldAnnotation.handleType() : annotation.handleType();
                char replaceChar = fieldAnnotation != null ? fieldAnnotation.replaceChar() : annotation.replaceChar();
                
                List<SensitiveWordResult> results = sensitiveWordFilter.findAll(text, matchType);
                
                if (!results.isEmpty()) {
                    String processed = switch (handleType) {
                        case EXCEPTION -> {
                            String message = StrUtil.format(annotation.message() + " [字段: {}, 敏感词: {}]", 
                                    field.getName(),
                                    results.stream().map(SensitiveWordResult::getWord).toList());
                            throw new SensitiveWordException(message, results);
                        }
                        case REPLACE -> sensitiveWordFilter.replace(text, replaceChar, matchType);
                        case HIGHLIGHT -> sensitiveWordFilter.highlight(text, 
                                properties.getHighlightStartTag(), 
                                properties.getHighlightEndTag(), 
                                matchType);
                        case DETECT_ONLY -> {
                            log.warn("字段 {} 检测到敏感词: {}", field.getName(), 
                                    results.stream().map(SensitiveWordResult::getWord).toList());
                            yield text;
                        }
                    };
                    
                    ReflectUtil.setFieldValue(obj, field, processed);
                }
            }
        }
    }
}
