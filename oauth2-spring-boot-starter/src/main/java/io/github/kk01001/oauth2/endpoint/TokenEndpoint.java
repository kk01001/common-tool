package io.github.kk01001.oauth2.endpoint;

import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author kk01001
 * @date 2025-02-22 14:31:00
 * @description Token端点
 */
@RestController
@RequestMapping("/oauth2/token")
public class TokenEndpoint {

    private final OAuth2AuthorizationService authorizationService;

    public TokenEndpoint(OAuth2AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    /**
     * 撤销访问令牌
     */
    @DeleteMapping("/revoke/{token}")
    public void revokeToken(@PathVariable String token) {
        OAuth2Authorization authorization = authorizationService.findByToken(
            token, OAuth2TokenType.ACCESS_TOKEN);
        
        if (authorization != null) {
            authorizationService.remove(authorization);
        }
    }
} 