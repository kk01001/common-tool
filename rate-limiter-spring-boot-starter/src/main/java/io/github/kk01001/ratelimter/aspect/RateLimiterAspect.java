package io.github.kk01001.ratelimter.aspect;

import cn.hutool.core.util.StrUtil;
import io.github.kk01001.ratelimter.core.RateLimiterFactory;
import io.github.kk01001.ratelimter.exception.RateLimitException;
import io.github.kk01001.ratelimter.model.Rule;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.CodeSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Objects;

/**
 * @author linshiqiang
 * @date 2024-09-06 21:42:00
 * @description
 */
@Aspect
@Component
public class RateLimiterAspect {

    private final BeanFactoryResolver beanFactoryResolver;

    private final RateLimiterFactory rateLimiterFactory;

    private final SpelExpressionParser parser = new SpelExpressionParser();

    @Autowired
    public RateLimiterAspect(ApplicationContext applicationContext, RateLimiterFactory rateLimiterFactory) {
        // 初始化 BeanFactoryResolver，只创建一次
        this.beanFactoryResolver = new BeanFactoryResolver(applicationContext);
        this.rateLimiterFactory = rateLimiterFactory;
    }

    @Around("@annotation(rateLimiter)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimiter rateLimiter) throws Throwable {
        if (StrUtil.isEmpty(rateLimiter.key())) {
            throw new RateLimitException("redis limiter key cannot be null");
        }

        // 创建SpEL上下文
        EvaluationContext context = getEvaluationContext(joinPoint);

        Rule rule = buildRule(rateLimiter, context);

        boolean access = rateLimiterFactory.tryAccess(rule);
        if (!access) {
            throw new RateLimitException("请求过于频繁，请稍后再试");
        }

        return joinPoint.proceed();
    }

    private Rule buildRule(RateLimiter rateLimiter, EvaluationContext context) {
        String ruledFunction = rateLimiter.ruleFunction();
        Rule rule = parseExpression(ruledFunction, context, Rule.class);
        if (Objects.nonNull(rule)) {
            return rule;
        }

        return Rule.builder()
                .rateLimiterType(rateLimiter.type())
                .redisClientType(rateLimiter.redisClientType())
                .maxRequests(getValue(rateLimiter.maxRequests(), parseExpression(rateLimiter.maxRequestsFunction(), context, Integer.class)))
                .windowTime(getValue(rateLimiter.windowTime(), parseExpression(rateLimiter.windowTimeFunction(), context, Integer.class)))
                .bucketCapacity(getValue(rateLimiter.bucketCapacity(), parseExpression(rateLimiter.bucketCapacityFunction(), context, Integer.class)))
                .tokenRate(getValue(rateLimiter.tokenRate(), parseExpression(rateLimiter.tokenRateFunction(), context, Integer.class)))
                .permits(getValue(rateLimiter.permits(), parseExpression(rateLimiter.permitsFunction(), context, Integer.class)))
                .key(parseExpression(rateLimiter.key(), context, String.class))
                .build();
    }

    private Integer getValue(Integer value, Integer valueFunction) {
        return Objects.isNull(valueFunction) ? value : valueFunction;
    }

    /**
     * 解析SpEL表达式
     */
    private <T> T parseExpression(String spEl, EvaluationContext context, Class<T> returnClassType) {
        if (StrUtil.isEmpty(spEl)) {
            return null;
        }
        // 解析SpEL表达式
        Expression expression = parser.parseExpression(spEl);
        return expression.getValue(context, returnClassType);
    }

    private EvaluationContext getEvaluationContext(ProceedingJoinPoint joinPoint) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        // 将方法的参数名和值添加到SpEL上下文中
        Object[] args = joinPoint.getArgs();
        String[] paramNames = ((CodeSignature) joinPoint.getSignature()).getParameterNames();

        Assert.notNull(paramNames, "paramNames is null, compiler code, please add -parameters");

        for (int i = 0; i < paramNames.length; i++) {
            context.setVariable(paramNames[i], args[i]);
        }
        context.setBeanResolver(beanFactoryResolver);
        return context;
    }

}
