package io.github.kk01001.oauth2.config;

import io.github.kk01001.oauth2.properties.OAuth2Properties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author kk01001
 * @date 2025-02-22 14:31:00
 * @description OAuth2自动配置类
 */
@Configuration
@EnableConfigurationProperties(OAuth2Properties.class)
@ConditionalOnProperty(prefix = "oauth2", name = "enabled", havingValue = "true", matchIfMissing = true)
@Import({
    OAuth2AuthorizationServerConfig.class,
    OAuth2ResourceServerConfig.class,
    OAuth2SecurityConfig.class
})
public class OAuth2AutoConfiguration {
} 