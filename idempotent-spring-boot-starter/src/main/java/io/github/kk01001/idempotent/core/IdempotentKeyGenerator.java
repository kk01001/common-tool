package io.github.kk01001.idempotent.core;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.CodeSignature;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.Assert;

/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description 幂等key生成器
 */
@Slf4j
public class IdempotentKeyGenerator {

    private final SpelExpressionParser parser = new SpelExpressionParser();

    /**
     * 生成幂等key
     */
    public String generateKey(String keyExpression, String keyPrefix, ProceedingJoinPoint joinPoint, 
                            BeanFactoryResolver beanFactoryResolver) {
        if (StrUtil.isEmpty(keyExpression)) {
            return keyPrefix + hashParams(joinPoint);
        }

        EvaluationContext context = getEvaluationContext(joinPoint, beanFactoryResolver);
        Expression expression = parser.parseExpression(keyExpression);
        return keyPrefix + SecureUtil.md5(expression.getValue(context, String.class));
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

    private EvaluationContext getEvaluationContext(ProceedingJoinPoint joinPoint, 
                                                 BeanFactoryResolver beanFactoryResolver) {
        StandardEvaluationContext context = new StandardEvaluationContext();
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