package io.github.kk01001.oauth2.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import io.github.kk01001.oauth2.authentication.AuthenticationProvider;
import io.github.kk01001.oauth2.authentication.UsernamePasswordAuthenticationProvider;
import io.github.kk01001.oauth2.authentication.VerificationCodeAuthenticationProvider;
import io.github.kk01001.oauth2.authentication.VerificationCodeService;
import io.github.kk01001.oauth2.authentication.social.SocialAuthenticationProvider;
import io.github.kk01001.oauth2.authentication.social.SocialUserService;

/**
 * @author kk01001
 * @date 2025-02-22 14:31:00
 * @description 认证管理器配置
 */
@Configuration
public class AuthenticationManagerConfig {

    /**
     * 配置认证管理器
     */
    @Bean
    public AuthenticationManager authenticationManager(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder,
            VerificationCodeService verificationCodeService,
            SocialUserService socialUserService) {
        
        List<org.springframework.security.authentication.AuthenticationProvider> providers = new ArrayList<>();
        
        // 用户名密码认证
        providers.add(new UsernamePasswordAuthenticationProvider(userDetailsService, passwordEncoder));
        
        // 验证码认证
        if (verificationCodeService != null) {
            providers.add(new VerificationCodeAuthenticationProvider(verificationCodeService));
        }
        
        // 社交登录认证
        if (socialUserService != null) {
            providers.add(new SocialAuthenticationProvider(socialUserService));
        }
        
        return new ProviderManager(providers);
    }
} 