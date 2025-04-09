package io.github.kk01001.design.pattern.statemachine.guard;

import io.github.kk01001.design.pattern.statemachine.exception.StateTransitionGuardException;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.Assert;

/**
 * @author kk01001
 * @date 2024-04-10 14:31:00
 * @description 基于SpEL表达式的守卫条件实现
 */
@Slf4j
public class SpELGuard<S, E, C> implements StateTransitionGuard<S, E, C> {

    private final Expression expression;
    private final String expressionString;

    @Setter
    private BeanFactoryResolver beanFactoryResolver;

    public SpELGuard(String expressionString) {
        Assert.hasText(expressionString, "SpEL expression must not be empty");
        ExpressionParser parser = new SpelExpressionParser();
        this.expressionString = expressionString;
        this.expression = parser.parseExpression(expressionString);
    }

    @Override
    public boolean evaluate(S source, E event, C context) {
        StandardEvaluationContext evalContext = new StandardEvaluationContext();
        evalContext.setVariable("source", source);
        evalContext.setVariable("event", event);
        evalContext.setVariable("context", context);

        if (beanFactoryResolver != null) {
            evalContext.setBeanResolver(beanFactoryResolver);
        }

        try {
            return Boolean.TRUE.equals(expression.getValue(evalContext, Boolean.class));
        } catch (EvaluationException e) {
            log.error("expressionString: {}, SpEL guard evaluation error:", expressionString, e);
            throw new StateTransitionGuardException(e.getMessage(), source, event);
        }
    }

    @Override
    public String getRejectionReason() {
        return String.format("SpEL守卫条件不满足: %s", expressionString);
    }
}
