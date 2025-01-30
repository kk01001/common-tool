package io.github.kk01001.common.desensitize.config;

import com.alibaba.fastjson.serializer.ValueFilter;
import io.github.kk01001.common.desensitize.fastjson.DesensitizeValueFilter;
import io.github.kk01001.common.desensitize.handler.DesensitizeHandlerFactory;
import io.github.kk01001.common.desensitize.properties.DesensitizeProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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