package io.github.kk01001.oauth2.authentication;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 * @author kk01001
 * @date 2025-02-22 14:31:00
 * @description 验证码认证提供者
 */
public class VerificationCodeAuthenticationProvider implements AuthenticationProvider {

    private final VerificationCodeService verificationCodeService;

    public VerificationCodeAuthenticationProvider(VerificationCodeService verificationCodeService) {
        this.verificationCodeService = verificationCodeService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        VerificationCodeAuthenticationToken token = (VerificationCodeAuthenticationToken) authentication;
        String principal = token.getPrincipal().toString(); // 手机号或邮箱
        String code = token.getCredentials().toString(); // 验证码

        if (verificationCodeService.verify(principal, code)) {
            return new VerificationCodeAuthenticationToken(
                principal, code, verificationCodeService.getUserAuthorities(principal));
        }

        throw new AuthenticationException("Invalid verification code") {};
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return VerificationCodeAuthenticationToken.class.isAssignableFrom(authentication);
    }
} 