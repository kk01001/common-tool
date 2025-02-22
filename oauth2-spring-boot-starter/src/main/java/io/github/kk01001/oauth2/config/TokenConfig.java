package io.github.kk01001.oauth2.config;

import java.time.Instant;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

import io.github.kk01001.oauth2.properties.OAuth2Properties;

/**
 * @author kk01001
 * @date 2025-02-22 14:31:00
 * @description Token配置类
 */
@Configuration
public class TokenConfig {

    private final OAuth2Properties properties;

    public TokenConfig(OAuth2Properties properties) {
        this.properties = properties;
    }

    /**
     * JWT Token定制器
     */
    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer() {
        return context -> {
            // 添加自定义声明
            context.getClaims().claims(claims -> {
                claims.put("iss", properties.getIssuerUrl());
                claims.put("iat", Instant.now().getEpochSecond());
                claims.put("client_id", context.getRegisteredClient().getClientId());
                
                // 如果是访问令牌，设置过期时间
                if (context.getTokenType().getValue().equals("access_token")) {
                    claims.put("exp", Instant.now()
                        .plus(properties.getAccessTokenValidityTime())
                        .getEpochSecond());
                }
                
                // 如果是刷新令牌，设置过期时间
                if (context.getTokenType().getValue().equals("refresh_token")) {
                    claims.put("exp", Instant.now()
                        .plus(properties.getRefreshTokenValidityTime())
                        .getEpochSecond());
                }
            });
        };
    }
} 