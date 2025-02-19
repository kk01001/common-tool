package io.github.kk01001.disruptor.config;

import com.lmax.disruptor.dsl.Disruptor;
import io.github.kk01001.disruptor.monitor.DisruptorMetrics;
import io.github.kk01001.disruptor.processor.DisruptorListenerProcessor;
import io.github.kk01001.disruptor.template.DisruptorTemplate;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
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
@ConditionalOnClass(Disruptor.class)
public class DisruptorAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DisruptorTemplate disruptorTemplate() {
        return new DisruptorTemplate();
    }

    @Bean
    public DisruptorListenerProcessor disruptorListenerProcessor(
            DisruptorTemplate disruptorTemplate) {
        return new DisruptorListenerProcessor(disruptorTemplate);
    }

    @Bean
    public DisruptorMetrics disruptorMetrics(DisruptorTemplate disruptorTemplate, MeterRegistry meterRegistry) {
        return new DisruptorMetrics(disruptorTemplate.getDisruptorMap(), meterRegistry);
    }
}