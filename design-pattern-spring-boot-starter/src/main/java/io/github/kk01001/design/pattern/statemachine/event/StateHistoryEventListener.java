package io.github.kk01001.design.pattern.statemachine.event;

import io.github.kk01001.design.pattern.statemachine.history.StateHistoryRepository;
import io.github.kk01001.design.pattern.statemachine.history.StateTransitionHistory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.event.EventListener;

/**
 * @author kk01001
 * @date 2024-04-07 14:31:00
 * @description 状态历史记录事件监听器
 */
@Slf4j
public class StateHistoryEventListener {

    private final ObjectProvider<StateHistoryRepository> historyRepositoryObjectProvider;

    public StateHistoryEventListener(ObjectProvider<StateHistoryRepository> historyRepositoryObjectProvider) {
        this.historyRepositoryObjectProvider = historyRepositoryObjectProvider;
    }

    /**
     * 监听状态转换事件
     */
    @EventListener
    public <S, E, C> void handleStateTransitionEvent(StateTransitionEvent<S, E, C> event) {
        try {
            recordTransition(event, event.isSuccess());
        } catch (Exception e) {
            log.error("处理状态转换事件失败: {}", event, e);
        }
    }

    private <S, E, C> void recordTransition(StateTransitionEvent<S, E, C> event, boolean isScucess) {
        StateHistoryRepository historyRepository = historyRepositoryObjectProvider.getIfAvailable();
        if (historyRepository == null) {
            log.debug("状态转换历史记录失败: 状态历史记录器未配置");
            return;
        }
        StateTransitionHistory<S, E> history = new StateTransitionHistory.Builder<S, E>()
                .machineName(event.getMachineName())
                .machineId(event.getMachineId())
                .sourceState(event.getSourceState())
                .targetState(event.getTargetState())
                .event(event.getEvent())
                .context(event.getContext())
                .success(isScucess)
                .failureReason(event.getFailureReason())
                .transitionTime(event.getTransitionTime())
                .build();

        historyRepository.save(history);
        log.debug("记录状态转换历史: {}", history);
    }

}