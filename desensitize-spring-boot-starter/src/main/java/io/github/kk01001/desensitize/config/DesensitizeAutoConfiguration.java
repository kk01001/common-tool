package io.github.kk01001.desensitize.config;

import io.github.kk01001.desensitize.handler.DesensitizeHandlerFactory;
import io.github.kk01001.desensitize.properties.DesensitizeProperties;
import io.github.kk01001.desensitize.util.DesensitizeUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description
 */
@Configuration
@EnableConfigurationProperties(DesensitizeProperties.class)
@ConditionalOnProperty(prefix = "desensitize", name = "enabled", havingValue = "true")
@Import(value = {FastJsonDesensitizeAutoConfiguration.class, JacksonDesensitizeAutoConfiguration.class})
public class DesensitizeAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public DesensitizeHandlerFactory desensitizeHandlerFactory(ApplicationContext applicationContext) {
        return new DesensitizeHandlerFactory(applicationContext);
    }

    @Bean
    public DesensitizeUtil desensitizeUtil() {
        return new DesensitizeUtil();
    }
   
}