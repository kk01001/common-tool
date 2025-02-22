package io.github.kk01001.oauth2.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.kk01001.oauth2.authentication.social.SocialAuthenticationProvider;
import io.github.kk01001.oauth2.authentication.social.SocialUserService;

/**
 * @author kk01001
 * @date 2025-02-22 14:31:00
 * @description 社交登录配置
 */
@Configuration
@ConditionalOnProperty(prefix = "oauth2.social", name = "enabled", havingValue = "true")
public class SocialLoginConfig {

    @Bean
    public SocialAuthenticationProvider socialAuthenticationProvider(SocialUserService socialUserService) {
        return new SocialAuthenticationProvider(socialUserService);
    }
} 