package io.github.archer099.desensitize.desensitize.config;

import com.fasterxml.jackson.databind.module.SimpleModule;
import io.github.archer099.desensitize.desensitize.handler.DesensitizeHandlerFactory;
import io.github.archer099.desensitize.desensitize.jackson.DesensitizeModule;
import io.github.archer099.desensitize.desensitize.jackson.DesensitizeSerializer;
import io.github.archer099.desensitize.desensitize.properties.DesensitizeProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description
 */
@Configuration
@EnableConfigurationProperties(DesensitizeProperties.class)
@ConditionalOnProperty(prefix = "desensitize", name = "enable-jackson", havingValue = "true")
public class JacksonDesensitizeAutoConfiguration {

    /**
     * 配置Jackson序列化脱敏
     */
    @Bean
    public DesensitizeModule desensitizeModule(DesensitizeHandlerFactory handlerFactory) {
        SimpleModule module = new SimpleModule();
        module.addSerializer(String.class, new DesensitizeSerializer(handlerFactory));
        return new DesensitizeModule(module);
    }
} 