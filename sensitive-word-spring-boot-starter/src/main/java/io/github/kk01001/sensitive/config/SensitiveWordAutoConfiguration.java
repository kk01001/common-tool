package io.github.kk01001.sensitive.config;

import io.github.kk01001.sensitive.core.DfaSensitiveWordFilter;
import io.github.kk01001.sensitive.core.SensitiveWordFilter;
import io.github.kk01001.sensitive.handler.SensitiveWordAspect;
import io.github.kk01001.sensitive.handler.SensitiveWordService;
import io.github.kk01001.sensitive.properties.SensitiveWordProperties;
import io.github.kk01001.sensitive.util.SensitiveWordUtil;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * @author kk01001
 * @date 2026-01-18 13:02:31
 * @description 敏感词自动配置类
 */
@AutoConfiguration
@EnableConfigurationProperties(SensitiveWordProperties.class)
@ConditionalOnProperty(prefix = "sensitive-word", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SensitiveWordAutoConfiguration {
    
    /**
     * 敏感词过滤器
     */
    @Bean
    @ConditionalOnMissingBean
    public SensitiveWordFilter sensitiveWordFilter(SensitiveWordProperties properties) {
        DfaSensitiveWordFilter filter = new DfaSensitiveWordFilter(properties.isIgnoreCase());
        filter.setSkipWhitespace(properties.isSkipWhitespace());
        filter.setSkipChars(properties.getSkipChars());
        
        // 设置为默认过滤器（供工具类使用）
        SensitiveWordUtil.setFilter(filter);
        
        return filter;
    }
    
    /**
     * 敏感词服务
     */
    @Bean
    @ConditionalOnMissingBean
    public SensitiveWordService sensitiveWordService(SensitiveWordFilter filter, 
                                                      SensitiveWordProperties properties) {
        return new SensitiveWordService(filter, properties);
    }
    
    /**
     * 敏感词 AOP 切面
     */
    @Bean
    @ConditionalOnMissingBean
    public SensitiveWordAspect sensitiveWordAspect(SensitiveWordFilter filter, 
                                                    SensitiveWordProperties properties) {
        return new SensitiveWordAspect(filter, properties);
    }
}
