package io.github.kk01001.disruptor.config;

import io.github.kk01001.disruptor.processor.DisruptorListenerProcessor;
import io.github.kk01001.disruptor.template.DisruptorTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author kk01001
 * @date 2025-02-19 10:15:47
 * @description Disruptor自动配置类，用于自动装配相关Bean
 */
@Configuration
@EnableConfigurationProperties(DisruptorProperties.class)
public class DisruptorAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DisruptorTemplate disruptorTemplate() {
        return new DisruptorTemplate();
    }

    @Bean
    public DisruptorListenerProcessor disruptorListenerProcessor(
            DisruptorTemplate disruptorTemplate,
            DisruptorProperties properties) {
        return new DisruptorListenerProcessor(disruptorTemplate, properties.getBufferSize());
    }
}