package io.github.kk01001.oauth2.authentication.social;

import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

/**
 * @author kk01001
 * @date 2025-02-22 14:31:00
 * @description 社交登录认证令牌
 */
public class SocialAuthenticationToken extends AbstractAuthenticationToken {

    private final String provider; // 提供商(如: github, wechat等)
    private final Object principal; // 社交用户唯一标识
    private Object credentials; // 社交令牌
    private Object details; // 社交用户详情

    public SocialAuthenticationToken(String provider, Object principal, Object credentials) {
        super(null);
        this.provider = provider;
        this.principal = principal;
        this.credentials = credentials;
        setAuthenticated(false);
    }

    public SocialAuthenticationToken(String provider, Object principal, Object credentials,
            Object details, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.provider = provider;
        this.principal = principal;
        this.credentials = credentials;
        this.details = details;
        super.setAuthenticated(true);
    }

    public String getProvider() {
        return this.provider;
    }

    @Override
    public Object getCredentials() {
        return this.credentials;
    }

    @Override
    public Object getPrincipal() {
        return this.principal;
    }

    public Object getDetails() {
        return this.details;
    }

    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
        this.credentials = null;
    }
} 