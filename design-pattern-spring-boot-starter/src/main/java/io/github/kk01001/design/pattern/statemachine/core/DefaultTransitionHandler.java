package io.github.kk01001.design.pattern.statemachine.core;

import io.github.kk01001.design.pattern.statemachine.annotations.StateTransition;
import io.github.kk01001.design.pattern.statemachine.guard.StateTransitionGuard;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author kk01001
 * @date 2024-04-07 14:31:00
 * @description 默认状态转换处理器
 */
public class DefaultTransitionHandler<S, E, C> implements StateTransitionHandler<S, E, C> {

    // 存储守卫条件
    private final List<StateTransitionGuard<S, E, C>> guards = new ArrayList<>();

    private final Method method;
    private final StateTransition annotation;
    private final Object bean;
    private final Class<?> stateType;
    private final Class<?> eventType;

    public DefaultTransitionHandler(Method method, StateTransition annotation, Object bean) {
        this.method = method;
        this.annotation = annotation;
        this.bean = bean;
        Parameter[] parameters = method.getParameters();
        this.stateType = parameters[0].getType();
        this.eventType = parameters[1].getType();
    }

    @SuppressWarnings("unchecked")
    @Override
    public S handleTransition(S from, E event, C context) {
        try {
            Object object = ReflectionUtils.invokeMethod(method, bean, from, event, context);
            return (S) object;
        } catch (Exception e) {
            throw new RuntimeException("Error executing state transition", e);
        }
    }

    @Override
    public S getSourceState() {
        return convertState(annotation.source(), "Error converting source state: ");
    }

    @Override
    public S getTargetState() {
        return convertState(annotation.target(), "Error converting target state: ");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public E getEvent() {
        try {
            return (E) Enum.valueOf((Class<? extends Enum>) eventType, annotation.event());
        } catch (Exception e) {
            throw new RuntimeException("Error converting event: " + annotation.event(), e);
        }
    }

    @Override
    public List<StateTransitionGuard<S, E, C>> getGuards() {
        return guards;
    }


    @SuppressWarnings({"unchecked", "rawtypes"})
    private S convertState(String annotation, String x) {
        try {
            return (S) Enum.valueOf((Class<? extends Enum>) stateType, annotation);
        } catch (Exception e) {
            throw new RuntimeException(x + annotation, e);
        }
    }
}
