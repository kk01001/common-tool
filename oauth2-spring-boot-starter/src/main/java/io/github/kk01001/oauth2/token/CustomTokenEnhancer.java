package io.github.kk01001.oauth2.token;

import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.stereotype.Component;

/**
 * @author kk01001
 * @date 2025-02-22 14:31:00
 * @description 自定义Token增强器
 */
@Component
public class CustomTokenEnhancer implements OAuth2TokenCustomizer<JwtEncodingContext> {
    
    @Override
    public void customize(JwtEncodingContext context) {
        // 添加自定义claims
        context.getClaims().claims(claims -> {
            claims.put("client_name", context.getRegisteredClient().getClientId());
            claims.put("timestamp", System.currentTimeMillis());
        });
    }
} 