package io.github.kk01001.idempotent.aspect;

import io.github.kk01001.idempotent.core.IdempotentKeyGenerator;
import io.github.kk01001.idempotent.core.RedisIdempotentExecutor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.ApplicationContext;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import java.lang.reflect.Method;

/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description 幂等切面
 */
@Slf4j
@Aspect
@Order(1)
@Component
public class IdempotentAspect {

    private final RedisIdempotentExecutor executor;
    private final IdempotentKeyGenerator keyGenerator;
    private final BeanFactoryResolver beanFactoryResolver;

    public IdempotentAspect(RedisIdempotentExecutor executor,
                           IdempotentKeyGenerator keyGenerator,
                           ApplicationContext applicationContext) {
        this.executor = executor;
        this.keyGenerator = keyGenerator;
        this.beanFactoryResolver = new BeanFactoryResolver(applicationContext);
    }

    @Around("@annotation(idempotent)")
    public Object checkIdempotent(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {
        // 只处理POST PUT接口
        if (!needCheck(joinPoint)) {
            return joinPoint.proceed();
        }

        // 生成幂等key并执行幂等性检查
        String key = keyGenerator.generateKey(idempotent.key(), idempotent.keyPrefix(), 
                                           joinPoint, beanFactoryResolver);
        executor.execute(key, idempotent.expire());

        // 执行目标方法
        return joinPoint.proceed();
    }

    private boolean needCheck(ProceedingJoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        return method.isAnnotationPresent(PostMapping.class) || 
               method.isAnnotationPresent(PutMapping.class);
    }
}
