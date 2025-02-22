package io.github.kk01001.oauth2.model;

import lombok.Data;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

import java.util.Set;

/**
 * @author kk01001
 * @date 2025-02-22 14:31:00
 * @description 客户端注册DTO
 */
@Data
public class ClientRegistrationDto {

    /**
     * 客户端ID
     */
    private String clientId;
    
    /**
     * 客户端密钥
     */
    private String clientSecret;
    
    /**
     * 客户端认证方式
     */
    private Set<ClientAuthenticationMethod> clientAuthenticationMethods;
    
    /**
     * 授权类型
     */
    private Set<AuthorizationGrantType> authorizationGrantTypes;
    
    /**
     * 重定向URI
     */
    private Set<String> redirectUris;
    
    /**
     * 授权范围
     */
    private Set<String> scopes;
} 