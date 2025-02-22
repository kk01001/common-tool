package io.github.kk01001.oauth2.endpoint;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.kk01001.oauth2.authentication.VerificationCodeAuthenticationToken;
import io.github.kk01001.oauth2.authentication.social.SocialAuthenticationToken;
import io.github.kk01001.oauth2.model.AuthenticationRequest;
import io.github.kk01001.oauth2.model.TokenResponse;
import lombok.RequiredArgsConstructor;

/**
 * @author kk01001
 * @date 2025-02-22 14:31:00
 * @description 认证端点
 */
@RestController
@RequestMapping("/oauth2/authenticate")
@RequiredArgsConstructor
public class AuthenticationEndpoint {

    private final AuthenticationManager authenticationManager;
    private final OAuth2TokenGenerator<?> tokenGenerator;

    /**
     * 登录认证
     */
    @PostMapping
    public TokenResponse authenticate(@RequestBody AuthenticationRequest request) {
        // 根据认证类型创建认证token
        Authentication authentication = createAuthenticationToken(request);
        
        // 认证
        Authentication authenticated = authenticationManager.authenticate(authentication);
        
        // 生成token
        OAuth2TokenType tokenType = OAuth2TokenType.ACCESS_TOKEN;
        var token = tokenGenerator.generate(authenticated, tokenType);
        
        return new TokenResponse(token.getTokenValue());
    }

    private Authentication createAuthenticationToken(AuthenticationRequest request) {
        return switch (request.getType()) {
            case PASSWORD -> new UsernamePasswordAuthenticationToken(
                request.getPrincipal(), request.getCredentials());
            case VERIFICATION_CODE -> new VerificationCodeAuthenticationToken(
                request.getPrincipal(), request.getCredentials());
            case SOCIAL -> new SocialAuthenticationToken(
                request.getProvider(), request.getPrincipal(), request.getCredentials());
        };
    }
} 