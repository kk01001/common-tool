package io.github.kk01001.oauth2.config;

import io.github.kk01001.oauth2.properties.OAuth2Properties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * @author kk01001
 * @date 2025-02-22 14:31:00
 * @description OAuth2资源服务器配置
 */
@Configuration
public class OAuth2ResourceServerConfig {

    /**
     * 资源服务器安全配置
     */
    @Bean
    SecurityFilterChain resourceServerSecurityFilterChain(HttpSecurity http) throws Exception {
        http.oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt);
        return http.build();
    }

    /**
     * JWT解码器
     */
    @Bean
    JwtDecoder jwtDecoder(OAuth2Properties properties) {
        byte[] keyBytes = properties.getJwtSigningKey().getBytes(StandardCharsets.UTF_8);
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "HMACSHA256");
        return NimbusJwtDecoder.withSecretKey(secretKey).build();
    }
} 