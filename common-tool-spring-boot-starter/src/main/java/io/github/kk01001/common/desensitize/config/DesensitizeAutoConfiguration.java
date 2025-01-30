package io.github.kk01001.common.desensitize.config;

import com.alibaba.fastjson.serializer.SerializeConfig;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.github.kk01001.common.desensitize.fastjson.DesensitizeValueFilter;
import io.github.kk01001.common.desensitize.handler.DesensitizeHandlerFactory;
import io.github.kk01001.common.desensitize.jackson.DesensitizeSerializer;
import io.github.kk01001.common.desensitize.properties.DesensitizeProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
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
    
    /**
     * 配置FastJson序列化脱敏
     */
    @Bean
    @ConditionalOnClass(SerializeConfig.class)
    @ConditionalOnProperty(prefix = "desensitize", name = "enable-fastjson", havingValue = "true")
    public DesensitizeValueFilter desensitizeValueFilter(DesensitizeHandlerFactory handlerFactory) {
        return new DesensitizeValueFilter(handlerFactory);
    }
} 