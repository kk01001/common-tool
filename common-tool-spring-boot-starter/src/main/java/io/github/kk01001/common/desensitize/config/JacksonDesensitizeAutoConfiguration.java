package io.github.kk01001.common.desensitize.config;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.github.kk01001.common.desensitize.handler.DesensitizeHandlerFactory;
import io.github.kk01001.common.desensitize.jackson.DesensitizeSerializer;
import io.github.kk01001.common.desensitize.properties.DesensitizeProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(DesensitizeProperties.class)
@ConditionalOnProperty(prefix = "desensitize", name = "enable-jackson", havingValue = "true", matchIfMissing = true)
public class JacksonDesensitizeAutoConfiguration {

    /**
     * 配置Jackson序列化脱敏
     */
    @Bean
    @ConditionalOnProperty(prefix = "desensitize", name = "enable-jackson", havingValue = "true", matchIfMissing = true)
    public Module desensitizeModule(DesensitizeHandlerFactory handlerFactory) {
        SimpleModule module = new SimpleModule();
        module.addSerializer(String.class, new DesensitizeSerializer(handlerFactory));
        return module;
    }
} 