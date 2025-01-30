package io.github.kk01001.common.desensitize.config;

import io.github.kk01001.common.desensitize.handler.DesensitizeHandlerFactory;
import io.github.kk01001.common.desensitize.properties.DesensitizeProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(DesensitizeProperties.class)
@ConditionalOnProperty(prefix = "desensitize", name = "enabled", havingValue = "true")
public class DesensitizeAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public DesensitizeHandlerFactory desensitizeHandlerFactory(ApplicationContext applicationContext) {
        return new DesensitizeHandlerFactory(applicationContext);
    }
   
}