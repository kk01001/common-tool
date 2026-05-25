package io.github.archer099.signature.handler;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * @author archer099
 * @date 2026-01-18 13:23:23
 * @description 签名过滤器，用于包装请求以支持重复读取请求体
 */
@Slf4j
public class SignatureFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        if (request instanceof HttpServletRequest httpRequest) {
            // 只对 POST/PUT/PATCH 请求进行包装
            String method = httpRequest.getMethod();
            String contentType = httpRequest.getContentType();
            
            if (("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method) 
                    || "PATCH".equalsIgnoreCase(method)) && contentType != null) {
                // 包装请求以支持重复读取
                RepeatableReadRequestWrapper wrappedRequest = new RepeatableReadRequestWrapper(httpRequest);
                chain.doFilter(wrappedRequest, response);
                return;
            }
        }
        
        chain.doFilter(request, response);
    }
}
