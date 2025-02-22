package io.github.kk01001.oauth2.authentication.social;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import io.github.kk01001.oauth2.authentication.AuthenticationProvider;

/**
 * @author kk01001
 * @date 2025-02-22 14:31:00
 * @description 社交登录认证提供者
 */
public class SocialAuthenticationProvider implements AuthenticationProvider {

    private final SocialUserService socialUserService;

    public SocialAuthenticationProvider(SocialUserService socialUserService) {
        this.socialUserService = socialUserService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        SocialAuthenticationToken token = (SocialAuthenticationToken) authentication;
        String provider = token.getProvider();
        String code = token.getCredentials().toString();

        try {
            // 获取社交用户信息
            SocialUserInfo userInfo = socialUserService.getUserInfo(provider, code);
            
            // 返回已认证的token
            return new SocialAuthenticationToken(
                provider,
                userInfo.getSocialId(),
                code,
                userInfo,
                socialUserService.getUserAuthorities(provider, userInfo.getSocialId())
            );
        } catch (Exception e) {
            throw new AuthenticationException("Failed to authenticate with " + provider) {};
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return SocialAuthenticationToken.class.isAssignableFrom(authentication);
    }
} 