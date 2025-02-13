package io.github.kk01001.desensitize.config;

import com.alibaba.fastjson.serializer.ValueFilter;
import io.github.kk01001.desensitize.fastjson.DesensitizeValueFilter;
import io.github.kk01001.desensitize.handler.DesensitizeHandlerFactory;
import io.github.kk01001.desensitize.properties.DesensitizeProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
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
@ConditionalOnProperty(prefix = "desensitize", name = "enable-fastjson", havingValue = "true")
public class FastJsonDesensitizeAutoConfiguration {

    /**
     * 配置FastJson序列化脱敏
     */
    @Bean
    @ConditionalOnClass(ValueFilter.class)
    public DesensitizeValueFilter desensitizeValueFilter(DesensitizeHandlerFactory handlerFactory) {
        return new DesensitizeValueFilter(handlerFactory);
    }
} 