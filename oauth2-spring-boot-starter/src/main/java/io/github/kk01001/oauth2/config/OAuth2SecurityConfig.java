package io.github.kk01001.oauth2.config;

import io.github.kk01001.oauth2.properties.OAuth2Properties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import java.util.UUID;

/**
 * @author kk01001
 * @date 2025-02-22 14:31:00
 * @description OAuth2安全配置
 */
@Configuration
@EnableWebSecurity
public class OAuth2SecurityConfig {

    /**
     * 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 安全过滤链配置
     */
    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> 
                authorize.anyRequest().authenticated()
            )
            .formLogin(form -> form.permitAll());
        return http.build();
    }

    /**
     * 用户详情服务
     */
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        var userDetails = User.builder()
            .username("admin")
            .password(passwordEncoder.encode("password"))
            .roles("ADMIN")
            .build();
        return new InMemoryUserDetailsManager(userDetails);
    }

    /**
     * 客户端注册信息仓库
     */
    @Bean
    public RegisteredClientRepository registeredClientRepository(OAuth2Properties properties, 
                                                              PasswordEncoder passwordEncoder) {
        return new InMemoryRegisteredClientRepository(
            properties.getClients().stream()
                .map(client -> RegisteredClient.withId(UUID.randomUUID().toString())
                    .clientId(client.getClientId())
                    .clientSecret(passwordEncoder.encode(client.getClientSecret()))
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                    .authorizationGrantTypes(grantTypes -> client.getAuthorizedGrantTypes()
                        .forEach(grantType -> grantTypes.add(new AuthorizationGrantType(grantType))))
                    .scopes(scopes -> client.getScopes().forEach(scopes::add))
                    .redirectUris(uris -> client.getRedirectUris().forEach(uris::add))
                    .build())
                .toList()
        );
    }
} 