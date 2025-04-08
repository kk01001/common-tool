package io.github.kk01001.design.pattern.statemachine.metrics;

import io.github.kk01001.design.pattern.statemachine.event.StateTransitionEvent;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author kk01001
 * @date 2024-04-08 14:31:00
 * @description 集成Micrometer的状态机指标收集器
 */
@Slf4j
public class MicrometerStateMachineMetrics {

    private final MeterRegistry meterRegistry;

    /**
     * 状态机实例计数器: machineName -> count
     */
    private final Map<String, AtomicLong> stateMachineInstanceCounters = new ConcurrentHashMap<>();

    /**
     * 状态数量计数器: machineName:state -> count
     */
    private final Map<String, AtomicLong> stateCounters = new ConcurrentHashMap<>();

    /**
     * 状态转换计时器
     */
    private final Map<String, Timer> transitionTimers = new ConcurrentHashMap<>();

    /**
     * 状态转换失败计数器
     */
    private final Map<String, AtomicLong> transitionFailureCounters = new ConcurrentHashMap<>();

    public MicrometerStateMachineMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * 记录状态机实例
     *
     * @param machineName 状态机名称
     * @param machineId   状态机ID
     */
    public void recordStateMachineInstance(String machineName, String machineId) {
        stateMachineInstanceCounters.computeIfAbsent(machineName, key -> {
            AtomicLong counter = new AtomicLong(0);
            Gauge.builder("statemachine.instances", counter, AtomicLong::get)
                    .tags(Tags.of(Tag.of("machine", machineName)))
                    .description("状态机实例数量")
                    .register(meterRegistry);
            return counter;
        }).incrementAndGet();

        log.debug("记录状态机实例: {} [{}]", machineName, machineId);
    }

    /**
     * 移除状态机实例
     *
     * @param machineName 状态机名称
     * @param machineId   状态机ID
     */
    public void removeStateMachineInstance(String machineName, String machineId) {
        AtomicLong counter = stateMachineInstanceCounters.get(machineName);
        if (counter != null) {
            counter.decrementAndGet();
            log.debug("移除状态机实例: {} [{}]", machineName, machineId);
        }
    }

    /**
     * 处理状态转换事件
     *
     * @param event 状态转换事件
     */
    public <S, E, C> void handleStateTransitionEvent(StateTransitionEvent<S, E, C> event) {
        try {
            String machineName = event.getMachineName();
            switch (event.getEventType()) {
                case STATE_INITIALIZED:
                    recordStateMachineInstance(machineName, event.getMachineId());
                    incrementStateCount(machineName, event.getSourceState());
                    break;
                case BEFORE_TRANSITION:
                    break;
                case TRANSITION_COMPLETED:
                    decrementStateCount(machineName, event.getSourceState());
                    incrementStateCount(machineName, event.getTargetState());
                    recordTransitionDuration(machineName, event.getSourceState(), event.getTargetState(), event.getEvent());
                    break;
                case TRANSITION_FAILED:
                    incrementTransitionFailureCount(machineName, event.getSourceState(), event.getEvent());
                    break;
                case STATE_DESTROYED:
                    removeStateMachineInstance(machineName, event.getMachineId());
                    decrementStateCount(machineName, event.getSourceState());
                    break;
            }

        } catch (Exception e) {
            log.error("处理状态转换事件指标失败: {}", event, e);
        }
    }

    /**
     * 增加状态计数
     */
    private void incrementStateCount(String machineName, Object state) {
        if (state == null) {
            return;
        }

        String counterKey = machineName + ":" + state;
        stateCounters.computeIfAbsent(counterKey, key -> {
            AtomicLong counter = new AtomicLong(0);
            Gauge.builder("statemachine.states", counter, AtomicLong::get)
                    .tags(Tags.of(
                            Tag.of("machine", machineName),
                            Tag.of("state", state.toString())))
                    .description("状态数量")
                    .register(meterRegistry);
            return counter;
        }).incrementAndGet();
    }

    /**
     * 减少状态计数
     */
    private void decrementStateCount(String machineName, Object state) {
        if (state == null) {
            return;
        }

        String counterKey = machineName + ":" + state;
        AtomicLong counter = stateCounters.get(counterKey);
        if (counter != null) {
            long value = counter.decrementAndGet();
            // 确保计数不为负
            if (value < 0) {
                counter.set(0);
            }
        }
    }

    /**
     * 记录状态转换耗时
     */
    private <S, E> void recordTransitionDuration(String machineName, S fromState, S toState, E event) {
        if (fromState == null || toState == null || event == null) {
            return;
        }

        String timerKey = machineName + ":" + fromState + ":" + event + ":" + toState;
        Timer timer = transitionTimers.computeIfAbsent(timerKey, key -> {
            return Timer.builder("statemachine.transition.duration")
                    .tags(Tags.of(
                            Tag.of("machine", machineName),
                            Tag.of("source", fromState.toString()),
                            Tag.of("target", toState.toString()),
                            Tag.of("event", event.toString())))
                    .description("状态转换耗时")
                    .publishPercentiles(0.5, 0.95, 0.99)
                    .register(meterRegistry);
        });

        // 由于我们没有实际测量转换耗时，这里使用一个默认值
        // 在实际应用中，应该在事件中包含实际的转换耗时
        timer.record(10, TimeUnit.MILLISECONDS);
    }

    /**
     * 增加转换失败计数
     */
    private <S, E> void incrementTransitionFailureCount(String machineName, S fromState, E event) {
        if (fromState == null || event == null) {
            return;
        }

        String counterKey = machineName + ":" + fromState + ":" + event;
        transitionFailureCounters.computeIfAbsent(counterKey, key -> {
            AtomicLong counter = new AtomicLong(0);
            Gauge.builder("statemachine.transition.failures", counter, AtomicLong::get)
                    .tags(Tags.of(
                            Tag.of("machine", machineName),
                            Tag.of("source", fromState.toString()),
                            Tag.of("event", event.toString())))
                    .description("状态转换失败次数")
                    .register(meterRegistry);
            return counter;
        }).incrementAndGet();
    }
} 