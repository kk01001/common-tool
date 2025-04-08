package io.github.kk01001.design.pattern.statemachine.guard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author kk01001
 * @date 2024-04-07 14:31:00
 * @description 复合状态转换守卫条件，可以组合多个条件
 */
public class CompositeStateTransitionGuard<S, E, C> implements StateTransitionGuard<S, E, C> {

    private final List<StateTransitionGuard<S, E, C>> guards = new ArrayList<>();
    private String rejectionReason;
    
    @SafeVarargs
    public CompositeStateTransitionGuard(StateTransitionGuard<S, E, C>... guards) {
        if (guards != null) {
            this.guards.addAll(Arrays.asList(guards));
        }
    }

    public void addGuard(StateTransitionGuard<S, E, C> guard) {
        this.guards.add(guard);
    }

    @Override
    public boolean evaluate(S sourceState, E event, C context) {
        for (StateTransitionGuard<S, E, C> guard : guards) {
            if (!guard.evaluate(sourceState, event, context)) {
                this.rejectionReason = guard.getRejectionReason();
                return false;
            }
        }
        return true;
    }

    @Override
    public String getRejectionReason() {
        return rejectionReason != null ? rejectionReason : "状态转换条件不满足";
    }
} 