package io.github.kk01001.disruptor.config;

import com.lmax.disruptor.dsl.Disruptor;
import io.github.kk01001.disruptor.monitor.DisruptorMetrics;
import io.github.kk01001.disruptor.processor.DisruptorListenerProcessor;
import io.github.kk01001.disruptor.template.DisruptorTemplate;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Role;

import static org.springframework.beans.factory.config.BeanDefinition.ROLE_INFRASTRUCTURE;

/**
 * @author kk01001
 * @date 2025-02-19 10:15:47
 * @description Disruptor自动配置类，用于自动装配相关Bean
 */
@Configuration
@EnableConfigurationProperties(DisruptorProperties.class)
@ConditionalOnClass(Disruptor.class)
@Role(ROLE_INFRASTRUCTURE)
public class DisruptorAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "disruptor", name = "enable-metrics", havingValue = "true")
    public DisruptorMetrics disruptorMetrics(MeterRegistry meterRegistry) {
        return new DisruptorMetrics(meterRegistry);
    }

    @Bean
    @ConditionalOnMissingBean
    public DisruptorTemplate disruptorTemplate(@Autowired(required = false) DisruptorMetrics disruptorMetrics) {
        return new DisruptorTemplate(disruptorMetrics);
    }

    @Bean
    public DisruptorListenerProcessor disruptorListenerProcessor(@Lazy DisruptorTemplate disruptorTemplate) {
        return new DisruptorListenerProcessor(disruptorTemplate);
    }

}