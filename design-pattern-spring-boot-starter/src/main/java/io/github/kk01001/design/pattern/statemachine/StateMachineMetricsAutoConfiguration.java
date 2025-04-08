package io.github.kk01001.design.pattern.statemachine;

import io.github.kk01001.design.pattern.statemachine.config.StateMachineProperties;
import io.github.kk01001.design.pattern.statemachine.event.StateMachineMetricsEventListener;
import io.github.kk01001.design.pattern.statemachine.metrics.MicrometerStateMachineMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author kk01001
 * @date 2024-04-08 14:31:00
 * @description 状态机指标自动配置
 */
@Configuration
@ConditionalOnClass(MeterRegistry.class)
@EnableConfigurationProperties(StateMachineProperties.class)
@ConditionalOnProperty(prefix = "state-machine.metrics", name = "enabled", havingValue = "true", matchIfMissing = true)
public class StateMachineMetricsAutoConfiguration {

    /**
     * 状态机Micrometer指标收集器
     */
    @Bean
    @ConditionalOnMissingBean
    public MicrometerStateMachineMetrics micrometerStateMachineMetrics(MeterRegistry meterRegistry) {
        return new MicrometerStateMachineMetrics(meterRegistry);
    }

    /**
     * 状态机指标事件监听器
     */
    @Bean
    @ConditionalOnMissingBean
    public StateMachineMetricsEventListener stateMachineMetricsEventListener(
            MicrometerStateMachineMetrics metrics) {
        return new StateMachineMetricsEventListener(metrics);
    }
} 