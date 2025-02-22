package io.github.kk01001.oauth2.authentication;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 * @author kk01001
 * @date 2025-02-22 14:31:00
 * @description 认证提供者接口
 */
public interface AuthenticationProvider {

    /**
     * 认证方法
     * @param authentication 认证信息
     * @return 认证结果
     * @throws AuthenticationException 认证异常
     */
    Authentication authenticate(Authentication authentication) throws AuthenticationException;

    /**
     * 是否支持此认证方式
     * @param authentication 认证类型
     * @return 是否支持
     */
    boolean supports(Class<?> authentication);
} 