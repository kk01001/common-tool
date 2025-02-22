package io.github.kk01001.oauth2.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * @author kk01001
 * @date 2025-02-22 14:31:00
 * @description OAuth2配置属性类
 */
@Data
@ConfigurationProperties(prefix = "oauth2")
public class OAuth2Properties {
    
    /**
     * 是否启用OAuth2
     */
    private boolean enabled = true;

    /**
     * 发行者URL
     */
    private String issuerUrl;

    /**
     * JWT签名密钥
     */
    private String jwtSigningKey;

    /**
     * 访问令牌有效期
     */
    private Duration accessTokenValidityTime = Duration.ofHours(2);

    /**
     * 刷新令牌有效期
     */
    private Duration refreshTokenValidityTime = Duration.ofDays(30);

    /**
     * 客户端配置列表
     */
    private List<ClientRegistration> clients = new ArrayList<>();

    /**
     * 客户端注册信息
     */
    @Data
    public static class ClientRegistration {
        /**
         * 客户端ID
         */
        private String clientId;
        
        /**
         * 客户端密钥
         */
        private String clientSecret;
        
        /**
         * 授权类型列表
         */
        private List<String> authorizedGrantTypes = new ArrayList<>();
        
        /**
         * 授权范围
         */
        private List<String> scopes = new ArrayList<>();
        
        /**
         * 重定向URI列表
         */
        private List<String> redirectUris = new ArrayList<>();
    }
} 