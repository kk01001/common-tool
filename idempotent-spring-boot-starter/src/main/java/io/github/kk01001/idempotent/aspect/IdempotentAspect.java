package io.github.kk01001.idempotent.aspect;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import io.github.kk01001.idempotent.exception.IdempotentException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.CodeSignature;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.core.annotation.Order;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * @author linshiqiang
 * @date 2024-12-03 11:29:00
 * @description
 */
@Aspect
@Component
@Order(1)
public class IdempotentAspect {

    private final RedissonClient redissonClient;

    private final BeanFactoryResolver beanFactoryResolver;

    private final SpelExpressionParser parser = new SpelExpressionParser();

    @Autowired
    public IdempotentAspect(RedissonClient redissonClient, ApplicationContext applicationContext) {
        this.redissonClient = redissonClient;
        // 初始化 BeanFactoryResolver，只创建一次
        this.beanFactoryResolver = new BeanFactoryResolver(applicationContext);
    }

    @Around("@annotation(idempotent)")
    public Object checkIdempotent(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {

        // 只处理POST PUT接口
        boolean isNeed = needCheck(joinPoint);
        if (!isNeed) {
            return joinPoint.proceed();
        }

        // 获取方法参数
        String key = getKey(joinPoint, idempotent);
        RBucket<Object> bucket = redissonClient.getBucket(key);
        boolean absent = bucket.setIfAbsent(System.currentTimeMillis(), Duration.of(idempotent.expire(), ChronoUnit.SECONDS));
        if (!absent) {
            throw new IdempotentException("This request has already been processed.");
        }

        // 执行目标方法
        return joinPoint.proceed();
    }

    private String getKey(ProceedingJoinPoint joinPoint, Idempotent idempotent) {
        String key = idempotent.key();
        if (StrUtil.isEmpty(key)) {
            return idempotent.keyPrefix() + hashParams(joinPoint);
        }
        EvaluationContext context = getEvaluationContext(joinPoint);
        // 解析SpEL表达式
        Expression expression = parser.parseExpression(key);
        return idempotent.keyPrefix() + SecureUtil.md5(expression.getValue(context, String.class));
    }

    private boolean needCheck(ProceedingJoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        return method.isAnnotationPresent(PostMapping.class) || method.isAnnotationPresent(PutMapping.class);
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

    private String hashParams(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        String[] paramNames = ((CodeSignature) joinPoint.getSignature()).getParameterNames();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < paramNames.length; i++) {
            builder.append(paramNames[i]).append(args[i]);
        }
        return SecureUtil.md5(builder.toString());
    }
}
