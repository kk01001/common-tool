package io.github.kk01001.design.pattern.statemachine;

import io.github.kk01001.design.pattern.statemachine.annotations.StateMachineDefinition;
import io.github.kk01001.design.pattern.statemachine.annotations.StateTransition;
import io.github.kk01001.design.pattern.statemachine.annotations.TransitionGuard;
import io.github.kk01001.design.pattern.statemachine.core.DefaultTransitionHandler;
import io.github.kk01001.design.pattern.statemachine.core.StateMachine;
import io.github.kk01001.design.pattern.statemachine.core.StateMachineBuilder;
import io.github.kk01001.design.pattern.statemachine.core.StateTransitionHandler;
import io.github.kk01001.design.pattern.statemachine.guard.StateTransitionGuard;
import io.github.kk01001.design.pattern.statemachine.persister.StatePersister;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author kk01001
 * @date 2024-04-07 14:31:00
 * @description 状态机工厂
 */
@Slf4j
public class StateMachineFactory implements ApplicationContextAware, InitializingBean {

    private final Map<String, StateMachine<?, ?, ?>> stateMachines = new ConcurrentHashMap<>();

    private ApplicationContext applicationContext;
    private BeanFactoryResolver beanFactoryResolver;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        this.beanFactoryResolver = new BeanFactoryResolver(applicationContext);
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

    @SuppressWarnings({"unchecked", "rawtypes"})
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
        log.info("注册状态机: {}, 初始状态: {}", name, definition.initialState());
    }

    @SuppressWarnings("unchecked")
    private <S, E, C> void registerTransitionHandler(Method method,
                                                     StateTransition annotation,
                                                     Object bean,
                                                     StateMachine<S, E, C> stateMachine) {

        StateTransitionHandler<S, E, C> handler = createHandler(method, annotation, bean);

        // 处理守卫条件注解
        TransitionGuard guardAnnotation = AnnotatedElementUtils.findMergedAnnotation(method, TransitionGuard.class);
        if (guardAnnotation != null) {
            if (guardAnnotation.value() != null) {
                Arrays.stream(guardAnnotation.value()).forEach(guardClass -> {
                    try {
                        // 从Spring容器中获取守卫条件Bean
                        StateTransitionGuard<S, E, C> guard = (StateTransitionGuard<S, E, C>)
                                applicationContext.getBean(guardClass);
                        handler.addGuard(guard);
                        log.info("为转换 {} -> {} [{}] 添加守卫条件: {}",
                                handler.getSourceState(), handler.getTargetState(),
                                handler.getEvent(), guardClass.getSimpleName());
                    } catch (BeansException e) {
                        // 如果容器中没有，则创建新实例
                        try {
                            StateTransitionGuard<S, E, C> guard = (StateTransitionGuard<S, E, C>)
                                    guardClass.getDeclaredConstructor().newInstance();
                            handler.addGuard(guard);
                            log.info("为转换 {} -> {} [{}] 添加守卫条件: {}",
                                    handler.getSourceState(), handler.getTargetState(),
                                    handler.getEvent(), guardClass.getSimpleName());
                        } catch (Exception ex) {
                            log.error("创建守卫条件实例失败: {}", guardClass.getName(), ex);
                        }
                    }
                });
            }

            // 处理SpEL表达式守卫
            if (guardAnnotation.spEL() != null) {
                for (String expressionString : guardAnnotation.spEL()) {
                    handler.withSpElGuard(expressionString, beanFactoryResolver);
                    log.info("为转换 {} -> {} [{}] 添加SpEL守卫条件: {}",
                            handler.getSourceState(), handler.getTargetState(),
                            handler.getEvent(), expressionString);
                }
            }
        }

        stateMachine.addTransitionHandler(handler);
    }

    private <S, E, C> StateTransitionHandler<S, E, C> createHandler(Method method,
                                                                    StateTransition annotation,
                                                                    Object bean) {
        return new DefaultTransitionHandler<>(method, annotation, bean);
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