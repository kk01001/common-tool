package io.github.kk01001.design.pattern.statemachine.event;

import io.github.kk01001.design.pattern.statemachine.metrics.MicrometerStateMachineMetrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;

/**
 * @author kk01001
 * @date 2024-04-08 14:31:00
 * @description 状态机指标事件监听器
 */
@Slf4j
public class StateMachineMetricsEventListener {

    private final MicrometerStateMachineMetrics metrics;

    public StateMachineMetricsEventListener(MicrometerStateMachineMetrics metrics) {
        this.metrics = metrics;
    }

    /**
     * 监听状态转换事件
     */
    @EventListener
    public <S, E, C> void handleStateTransitionEvent(StateTransitionEvent<S, E, C> event) {
        try {
            metrics.handleStateTransitionEvent(event);
        } catch (Exception e) {
            log.error("处理状态转换事件指标失败: {}", event, e);
        }
    }
} 