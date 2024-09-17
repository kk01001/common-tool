package io.github.kk01001.lock.aspect;

import cn.hutool.core.util.StrUtil;
import io.github.kk01001.lock.core.LockFactory;
import io.github.kk01001.lock.exception.LockException;
import io.github.kk01001.lock.model.LockRule;
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
public class LockAspect {

    private final BeanFactoryResolver beanFactoryResolver;

    private final LockFactory lockFactory;

    private final SpelExpressionParser parser = new SpelExpressionParser();

    @Autowired
    public LockAspect(ApplicationContext applicationContext, LockFactory lockFactory) {
        // 初始化 BeanFactoryResolver，只创建一次
        this.beanFactoryResolver = new BeanFactoryResolver(applicationContext);
        this.lockFactory = lockFactory;
    }

    @Around("@annotation(lock)")
    public Object around(ProceedingJoinPoint joinPoint, Lock lock) throws Throwable {
        if (StrUtil.isEmpty(lock.key())) {
            throw new LockException("lock key cannot be null");
        }

        // 创建SpEL上下文
        EvaluationContext context = getEvaluationContext(joinPoint);

        LockRule lockRule = buildRule(lock, context);

        try {
            // 阻塞
            if (Boolean.TRUE.equals(lockRule.getBlock())) {
                lockFactory.lock(lockRule);
                return joinPoint.proceed();
            }

            // 非阻塞
            boolean access = lockFactory.tryLock(lockRule);
            if (!access) {
                throw new LockException("获取锁失败，请稍后再试");
            }
            return joinPoint.proceed();
        } finally {
            lockFactory.unlock(lockRule);
        }
    }

    private LockRule buildRule(Lock lock, EvaluationContext context) {
        String ruledFunction = lock.ruleFunction();
        LockRule lockRule = parseExpression(ruledFunction, context, LockRule.class);
        if (Objects.nonNull(lockRule)) {
            return lockRule;
        }

        return LockRule.builder()
                .enable(lock.enable())
                .block(lock.block())
                .lockType(lock.type())
                .redisClientType(lock.redisClientType())
                .key(parseExpression(lock.key(), context, String.class))
                .permits(getValue(lock.permits(), parseExpression(lock.permitsFunction(), context, Integer.class)))
                .fair(lock.fair())
                .timeout(lock.timeout())
                .timeUnit(lock.timeunit())
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
