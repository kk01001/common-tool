package io.github.kk01001.signature.handler;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import io.github.kk01001.signature.annotation.IgnoreSignature;
import io.github.kk01001.signature.annotation.SignatureVerify;
import io.github.kk01001.signature.core.SignatureException;
import io.github.kk01001.signature.core.SignatureResult;
import io.github.kk01001.signature.properties.SignatureProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author kk01001
 * @date 2026-01-18 13:23:23
 * @description 签名验证拦截器
 */
@Slf4j
@RequiredArgsConstructor
public class SignatureInterceptor implements HandlerInterceptor {
    
    private final SignatureProperties properties;
    private final SignatureHandler signatureHandler;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 检查是否是 HandlerMethod
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        
        // 检查是否需要验证签名
        if (!shouldVerify(request, handlerMethod)) {
            return true;
        }
        
        // 获取签名参数
        String appKey = getParameter(request, properties.getAppKeyHeader(), properties.getAppKeyParam());
        String signature = getParameter(request, properties.getSignatureHeader(), properties.getSignatureParam());
        String timestamp = getParameter(request, properties.getTimestampHeader(), properties.getTimestampParam());
        String nonce = getParameter(request, properties.getNonceHeader(), properties.getNonceParam());
        
        // 获取请求参数
        Map<String, String> params = getRequestParams(request);
        
        // 移除签名相关参数
        params.remove(properties.getSignatureParam());
        
        // 获取是否需要验证 body
        SignatureVerify annotation = handlerMethod.getMethodAnnotation(SignatureVerify.class);
        if (annotation == null) {
            annotation = handlerMethod.getBeanType().getAnnotation(SignatureVerify.class);
        }
        
        String body = null;
        boolean parseJsonBody = false;
        if (annotation != null && annotation.verifyBody()) {
            body = getRequestBody(request);
            parseJsonBody = annotation.parseJsonBody();
        }
        
        // 验证签名
        SignatureResult result = signatureHandler.verify(appKey, signature, timestamp, nonce, params, body, parseJsonBody);
        
        if (!result.isValid()) {
            handleFailure(response, result);
            return false;
        }
        
        // 将验证结果存入请求属性
        request.setAttribute("signatureResult", result);
        request.setAttribute("signatureAppKey", appKey);
        
        return true;
    }
    
    /**
     * 判断是否需要验证签名
     */
    private boolean shouldVerify(HttpServletRequest request, HandlerMethod handlerMethod) {
        String path = request.getRequestURI();
        
        // 检查排除路径
        for (String excludePath : properties.getExcludePaths()) {
            if (pathMatcher.match(excludePath, path)) {
                return false;
            }
        }
        
        // 检查 @IgnoreSignature 注解
        if (handlerMethod.hasMethodAnnotation(IgnoreSignature.class) || 
            handlerMethod.getBeanType().isAnnotationPresent(IgnoreSignature.class)) {
            return false;
        }
        
        // 检查 @SignatureVerify 注解
        SignatureVerify methodAnnotation = handlerMethod.getMethodAnnotation(SignatureVerify.class);
        if (methodAnnotation != null) {
            return methodAnnotation.enabled();
        }
        
        SignatureVerify classAnnotation = handlerMethod.getBeanType().getAnnotation(SignatureVerify.class);
        if (classAnnotation != null) {
            return classAnnotation.enabled();
        }
        
        // 检查包含路径
        if (!properties.getIncludePaths().isEmpty()) {
            for (String includePath : properties.getIncludePaths()) {
                if (pathMatcher.match(includePath, path)) {
                    return true;
                }
            }
            return false;
        }
        
        // 默认需要验证
        return true;
    }
    
    /**
     * 获取参数（优先从请求头获取，其次从请求参数获取）
     */
    private String getParameter(HttpServletRequest request, String headerName, String paramName) {
        String value = request.getHeader(headerName);
        if (StrUtil.isBlank(value) && properties.isAllowQueryParams()) {
            value = request.getParameter(paramName);
        }
        return value;
    }
    
    /**
     * 获取请求参数
     */
    private Map<String, String> getRequestParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String name = paramNames.nextElement();
            String value = request.getParameter(name);
            if (StrUtil.isNotBlank(value)) {
                params.put(name, value);
            }
        }
        return params;
    }
    
    /**
     * 获取请求体
     */
    private String getRequestBody(HttpServletRequest request) {
        try {
            // 如果是包装后的请求，直接获取缓存的body
            if (request instanceof RepeatableReadRequestWrapper wrapper) {
                return wrapper.getBody();
            }
            return IoUtil.read(request.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("读取请求体失败", e);
            return null;
        }
    }
    
    /**
     * 处理验证失败
     */
    private void handleFailure(HttpServletResponse response, SignatureResult result) throws IOException {
        SignatureProperties.Response respConfig = properties.getResponse();
        
        response.setStatus(respConfig.getStatus());
        response.setContentType(respConfig.getContentType());
        
        String body = respConfig.getBody()
                .replace("${errorCode}", result.getErrorCode())
                .replace("${errorMessage}", result.getErrorMessage());
        
        response.getWriter().write(body);
        
        log.warn("签名验证失败 - errorCode: {}, errorMessage: {}", 
                result.getErrorCode(), result.getErrorMessage());
    }
}
