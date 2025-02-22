package io.github.kk01001.oauth2.model;

import lombok.Data;

/**
 * @author kk01001
 * @date 2025-02-22 14:31:00
 * @description 认证请求
 */
@Data
public class AuthenticationRequest {
    
    /**
     * 认证类型
     */
    private AuthenticationType type;
    
    /**
     * 认证主体(用户名/手机号/邮箱)
     */
    private String principal;
    
    /**
     * 认证凭证(密码/验证码/第三方token)
     */
    private String credentials;
    
    /**
     * 社交登录提供商
     */
    private String provider;
}

public enum AuthenticationType {
    PASSWORD,
    VERIFICATION_CODE,
    SOCIAL
} 