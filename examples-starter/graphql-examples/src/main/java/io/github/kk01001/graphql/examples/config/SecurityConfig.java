package io.github.kk01001.graphql.examples.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author linshiqiang
 * @date 2025-01-08 15:25:00
 * @description Security Configuration for GraphQL
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // Allow all HTTP requests, control access in GraphQL resolvers
                )
                .addFilterBefore(new PreAuthFilter(), UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /**
     * Mock Pre-Authentication Filter
     * Simulates a scenario where a Gateway passes the User ID in a header.
     */
    public static class PreAuthFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
            String userId = request.getHeader("X-User-Id");
            if (userId != null) {
                try {
                    Long id = Long.parseLong(userId);
                    String rolesHeader = request.getHeader("X-User-Roles");
                    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    if (rolesHeader != null && !rolesHeader.isBlank()) {
                        String[] roles = rolesHeader.split(",");
                        for (String role : roles) {
                            authorities.add(new SimpleGrantedAuthority(role.trim()));
                        }
                    } else {
                        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                    }

                    // In a real app, you might load more details or roles here
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            id, // Principal
                            null, // Credentials
                            authorities
                    );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } catch (NumberFormatException e) {
                    // Ignore invalid IDs
                }
            }
            filterChain.doFilter(request, response);
        }
    }
}
