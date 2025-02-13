package io.github.kk01001.desensitize.config;

import com.fasterxml.jackson.databind.module.SimpleModule;
import io.github.kk01001.desensitize.handler.DesensitizeHandlerFactory;
import io.github.kk01001.desensitize.jackson.DesensitizeModule;
import io.github.kk01001.desensitize.jackson.DesensitizeSerializer;
import io.github.kk01001.desensitize.properties.DesensitizeProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description
 */
@Configuration
@EnableConfigurationProperties(DesensitizeProperties.class)
@ConditionalOnProperty(prefix = "desensitize", name = "enable-jackson", havingValue = "true", matchIfMissing = true)
public class JacksonDesensitizeAutoConfiguration {

    /**
     * 配置Jackson序列化脱敏
     */
    @Bean
    @ConditionalOnProperty(prefix = "desensitize", name = "enable-jackson", havingValue = "true", matchIfMissing = true)
    public DesensitizeModule desensitizeModule(DesensitizeHandlerFactory handlerFactory) {
        SimpleModule module = new SimpleModule();
        module.addSerializer(String.class, new DesensitizeSerializer(handlerFactory));
        return new DesensitizeModule(module);
    }
} 