package io.github.kk01001.oauth2.authentication.social;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;

/**
 * @author kk01001
 * @date 2025-02-22 14:31:00
 * @description 社交用户服务接口
 */
public interface SocialUserService {

    /**
     * 获取社交用户信息
     * @param provider 提供商
     * @param code 授权码
     * @return 社交用户信息
     */
    SocialUserInfo getUserInfo(String provider, String code);

    /**
     * 绑定社交账号
     * @param provider 提供商
     * @param socialId 社交ID
     * @param userId 用户ID
     */
    void bindSocialUser(String provider, String socialId, String userId);

    /**
     * 解绑社交账号
     * @param provider 提供商
     * @param userId 用户ID
     */
    void unbindSocialUser(String provider, String userId);

    /**
     * 获取用户权限
     * @param provider 提供商
     * @param socialId 社交ID
     * @return 权限集合
     */
    Collection<? extends GrantedAuthority> getUserAuthorities(String provider, String socialId);
} 