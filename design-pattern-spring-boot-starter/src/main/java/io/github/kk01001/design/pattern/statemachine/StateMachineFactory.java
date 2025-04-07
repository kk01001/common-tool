package io.github.kk01001.design.pattern.statemachine;

import io.github.kk01001.design.pattern.statemachine.annotations.StateMachineDefinition;
import io.github.kk01001.design.pattern.statemachine.annotations.StateTransition;
import io.github.kk01001.design.pattern.statemachine.core.StateMachine;
import io.github.kk01001.design.pattern.statemachine.core.StateMachineBuilder;
import io.github.kk01001.design.pattern.statemachine.core.StateTransitionHandler;
import io.github.kk01001.design.pattern.statemachine.persister.StatePersister;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author kk01001
 * @date 2024-04-07 14:31:00
 * @description 状态机工厂
 */
public class StateMachineFactory implements ApplicationContextAware, InitializingBean {

    private ApplicationContext applicationContext;
    private final Map<String, StateMachine<?, ?, ?>> stateMachines = new ConcurrentHashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        // 扫描所有带有@StateMachineDefinition注解的Bean
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(StateMachineDefinition.class);
        beans.forEach((beanName, bean) -> {
            StateMachineDefinition definition = AnnotationUtils.findAnnotation(bean.getClass(), StateMachineDefinition.class);
            if (definition != null) {
                createAndRegisterStateMachine(definition, bean);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private <S, E, C> void createAndRegisterStateMachine(StateMachineDefinition definition, Object bean) {
        String name = definition.name();
        Class<? extends Enum> stateClass = definition.stateClass();
        Assert.hasText(name, "StateMachine name must not be empty");
        Assert.notNull(stateClass, "State class must not be null");
        Assert.isTrue(stateClass.isEnum(), "State class must be an enum type");


        StateMachineBuilder<S, E, C> builder = new StateMachineBuilder<>();
        StateMachine<S, E, C> stateMachine = builder
                .setMachineName(name)
                .setInitialState((S) Enum.valueOf(stateClass, definition.initialState()))
                .setPersister((StatePersister<S, C>) applicationContext.getBean(StatePersister.class))
                .build();

        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);

        // 处理状态转换注解
        Map<Method, StateTransition> annotatedMethods = MethodIntrospector.selectMethods(targetClass,
                (MethodIntrospector.MetadataLookup<StateTransition>) method ->
                        AnnotatedElementUtils.findMergedAnnotation(method, StateTransition.class));

        if (!annotatedMethods.isEmpty()) {
            // 获取状态机定义
            StateMachineDefinition machineDefinition = AnnotatedElementUtils.findMergedAnnotation(targetClass, StateMachineDefinition.class);
            if (machineDefinition != null) {
                // 注册状态转换处理器
                annotatedMethods.forEach((method, annotation) -> {
                    registerTransitionHandler(method, annotation, bean, stateMachine);
                });
            }
        }

        stateMachines.put(name, stateMachine);
    }

    @SuppressWarnings("unchecked")
    private <S, E, C> void registerTransitionHandler(Method method,
                                                     StateTransition annotation,
                                                     Object bean,
                                                     StateMachine<S, E, C> stateMachine) {
        // 获取方法参数类型
        Parameter[] parameters = method.getParameters();
        Class<S> stateType = (Class<S>) parameters[0].getType();
        Class<E> eventType = (Class<E>) parameters[1].getType();

        StateTransitionHandler<S, E, C> handler = new StateTransitionHandler<>() {
            @Override
            @SuppressWarnings("unchecked")
            public S handleTransition(S from, E event, C context) {
                try {
                    Object object = ReflectionUtils.invokeMethod(method, bean, from, event, context);
                    return (S) object;
                } catch (Exception e) {
                    throw new RuntimeException("Error executing state transition", e);
                }
            }

            @Override
            @SuppressWarnings("unchecked")
            public S getSourceState() {
                try {
                    // 使用枚举的valueOf方法将字符串转换为枚举
                    return (S) Enum.valueOf((Class<? extends Enum>) stateType, annotation.source());
                } catch (Exception e) {
                    throw new RuntimeException("Error converting source state: " + annotation.source(), e);
                }
            }

            @Override
            @SuppressWarnings("unchecked")
            public S getTargetState() {
                try {
                    return (S) Enum.valueOf((Class<? extends Enum>) stateType, annotation.target());
                } catch (Exception e) {
                    throw new RuntimeException("Error converting target state: " + annotation.target(), e);
                }
            }

            @Override
            @SuppressWarnings("unchecked")
            public E getEvent() {
                try {
                    return (E) Enum.valueOf((Class<? extends Enum>) eventType, annotation.event());
                } catch (Exception e) {
                    throw new RuntimeException("Error converting event: " + annotation.event(), e);
                }
            }
        };

        stateMachine.addTransitionHandler(handler);
    }

    /**
     * 获取状态机实例
     *
     * @param name 状态机名称
     * @return 状态机实例
     */
    @SuppressWarnings("unchecked")
    public <S, E, C> StateMachine<S, E, C> getStateMachine(String name) {
        StateMachine<?, ?, ?> machine = stateMachines.get(name);
        Assert.notNull(machine, "No StateMachine found with name: " + name);
        return (StateMachine<S, E, C>) machine;
    }

    /**
     * 获取状态机实例
     *
     * @param clazz 状态机定义类
     * @return 状态机实例
     */
    public <S, E, C> StateMachine<S, E, C> getStateMachine(Class<?> clazz) {
        StateMachineDefinition definition = AnnotationUtils.findAnnotation(clazz, StateMachineDefinition.class);
        Assert.notNull(definition, "No StateMachineDefinition annotation found on class: " + clazz.getName());
        return getStateMachine(definition.name());
    }
} 