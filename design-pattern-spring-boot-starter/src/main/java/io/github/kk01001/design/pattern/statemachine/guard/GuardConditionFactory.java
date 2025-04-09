package io.github.kk01001.design.pattern.statemachine.guard;

import org.springframework.context.expression.BeanFactoryResolver;

/**
 * @author kk01001
 * @date 2024-04-10 14:31:00
 * @description 守卫条件工厂，支持创建SpEL表达式守卫
 */
public class GuardConditionFactory {

    /**
     * 创建SpEL表达式守卫
     *
     * @param expression SpEL表达式
     * @return 守卫条件实例
     */
    public static <S, E, C> StateTransitionGuard<S, E, C> createSpElGuard(String expression) {
        return new SpELGuard<>(expression);
    }

    /**
     * 创建SpEL表达式守卫(支持Spring Bean引用)
     *
     * @param expression  SpEL表达式
     * @param beanFactory Spring Bean工厂
     * @return 守卫条件实例
     */
    public static <S, E, C> StateTransitionGuard<S, E, C> createSpElGuard(
            String expression,
            BeanFactoryResolver beanFactory) {
        SpELGuard<S, E, C> guard = new SpELGuard<>(expression);
        guard.setBeanFactoryResolver(beanFactory);
        return guard;
    }
}