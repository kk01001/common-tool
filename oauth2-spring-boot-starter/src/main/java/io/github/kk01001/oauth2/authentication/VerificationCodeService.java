package io.github.kk01001.oauth2.authentication;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;

/**
 * @author kk01001
 * @date 2025-02-22 14:31:00
 * @description 验证码服务接口
 */
public interface VerificationCodeService {

    /**
     * 发送验证码
     * @param principal 手机号或邮箱
     * @return 是否发送成功
     */
    boolean sendCode(String principal);

    /**
     * 验证验证码
     * @param principal 手机号或邮箱
     * @param code 验证码
     * @return 是否验证成功
     */
    boolean verify(String principal, String code);

    /**
     * 获取用户权限
     * @param principal 手机号或邮箱
     * @return 权限集合
     */
    Collection<? extends GrantedAuthority> getUserAuthorities(String principal);
} 