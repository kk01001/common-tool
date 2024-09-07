package io.github.kk01001.ratelimter.aspect;

import cn.hutool.core.util.StrUtil;
import io.github.kk01001.ratelimter.core.RateLimiterFactory;
import io.github.kk01001.ratelimter.exception.RateLimitException;
import io.github.kk01001.ratelimter.model.Rule;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

/**
 * @author linshiqiang
 * @date 2024-09-06 21:42:00
 * @description
 */
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimiterAspect {

    private final RateLimiterFactory rateLimiterFactory;

    private final SpelExpressionParser parser = new SpelExpressionParser();

    private final DefaultParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

    @Around("@annotation(rateLimiter)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimiter rateLimiter) throws Throwable {
        if (StrUtil.isEmpty(rateLimiter.key())) {
            throw new RateLimitException("redis limiter key cannot be null");
        }

        Rule rule = buildRule(joinPoint, rateLimiter);

        boolean access = rateLimiterFactory.tryAccess(rule);
        if (!access) {
            throw new RateLimitException("请求过于频繁，请稍后再试");
        }

        return joinPoint.proceed();
    }

    private Rule buildRule(ProceedingJoinPoint joinPoint, RateLimiter rateLimiter) {
        String key = parseLimiterKey(rateLimiter.key(), joinPoint);
        return Rule.builder()
                .rateLimiterType(rateLimiter.type())
                .maxRequests(rateLimiter.maxRequests())
                .windowTime(rateLimiter.windowTime())
                .bucketCapacity(rateLimiter.bucketCapacity())
                .tokenRate(rateLimiter.tokenRate())
                .permits(rateLimiter.permits())
                .key(key)
                .build();
    }


    /**
     * 解析SpEL表达式获取限流器的key
     */
    private String parseLimiterKey(String spEl, ProceedingJoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();

        // 创建SpEL解析器
        ExpressionParser parser = new SpelExpressionParser();

        // 创建SpEL上下文
        EvaluationContext context = new StandardEvaluationContext();

        // 将方法的参数名和值添加到SpEL上下文中
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < args.length; i++) {
            context.setVariable(args[i].getClass().getSimpleName(), args[i]);
        }

        // 解析SpEL表达式
        Expression expression = parser.parseExpression(spEl);
        return expression.getValue(context, String.class);
    }

    private String parseKey(String key, ProceedingJoinPoint joinPoint) {
        if (StringUtils.hasText(key)) {
            Method method = getMethod(joinPoint);
            EvaluationContext context = new MethodBasedEvaluationContext(null, method, joinPoint.getArgs(), nameDiscoverer);
            return parser.parseExpression(key).getValue(context, String.class);
        }
        return joinPoint.getSignature().toShortString();  // 默认方法签名作为key
    }

    private Method getMethod(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        if (method.getDeclaringClass().isInterface()) {
            try {
                method = joinPoint.getTarget().getClass().getDeclaredMethod(signature.getName(),
                        method.getParameterTypes());
            } catch (Exception e) {
            }
        }
        return method;
    }
}
