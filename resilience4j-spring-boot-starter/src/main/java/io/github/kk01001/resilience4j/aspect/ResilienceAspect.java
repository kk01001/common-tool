package io.github.kk01001.resilience4j.aspect;

import cn.hutool.core.util.StrUtil;
import io.github.kk01001.resilience4j.annotation.*;
import io.github.kk01001.resilience4j.handler.FallbackHandler;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Resilience4j 切面
 *
 * @author kk01001
 */
@Aspect
@Component
public class ResilienceAspect {

    private static final Logger log = LoggerFactory.getLogger(ResilienceAspect.class);

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RateLimiterRegistry rateLimiterRegistry;
    private final RetryRegistry retryRegistry;
    private final BulkheadRegistry bulkheadRegistry;
    private final TimeLimiterRegistry timeLimiterRegistry;

    public ResilienceAspect(CircuitBreakerRegistry circuitBreakerRegistry,
                           RateLimiterRegistry rateLimiterRegistry,
                           RetryRegistry retryRegistry,
                           BulkheadRegistry bulkheadRegistry,
                           TimeLimiterRegistry timeLimiterRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.rateLimiterRegistry = rateLimiterRegistry;
        this.retryRegistry = retryRegistry;
        this.bulkheadRegistry = bulkheadRegistry;
        this.timeLimiterRegistry = timeLimiterRegistry;
    }

    @Around("@annotation(circuitBreaker)")
    public Object aroundCircuitBreaker(ProceedingJoinPoint joinPoint, CircuitBreaker circuitBreaker) throws Throwable {
        String name = getName(joinPoint, circuitBreaker.name());
        io.github.resilience4j.circuitbreaker.CircuitBreaker breaker = circuitBreakerRegistry.circuitBreaker(name);

        try {
            return breaker.executeSupplier(() -> {
                try {
                    return joinPoint.proceed();
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            log.warn("Circuit breaker [{}] triggered", name, e);
            return FallbackHandler.handle(joinPoint, circuitBreaker.fallbackStrategy(), 
                circuitBreaker.fallbackMethod(), circuitBreaker.fallbackValue(), e);
        }
    }

    @Around("@annotation(rateLimiter)")
    public Object aroundRateLimiter(ProceedingJoinPoint joinPoint, RateLimiter rateLimiter) throws Throwable {
        String name = getName(joinPoint, rateLimiter.name());
        io.github.resilience4j.ratelimiter.RateLimiter limiter = rateLimiterRegistry.rateLimiter(name);

        try {
            return limiter.executeSupplier(() -> {
                try {
                    return joinPoint.proceed();
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            log.warn("Rate limiter [{}] triggered", name, e);
            return FallbackHandler.handle(joinPoint, rateLimiter.fallbackStrategy(), 
                rateLimiter.fallbackMethod(), rateLimiter.fallbackValue(), e);
        }
    }

    @Around("@annotation(retry)")
    public Object aroundRetry(ProceedingJoinPoint joinPoint, Retry retry) throws Throwable {
        String name = getName(joinPoint, retry.name());
        io.github.resilience4j.retry.Retry retryInstance = retryRegistry.retry(name);

        try {
            return retryInstance.executeSupplier(() -> {
                try {
                    return joinPoint.proceed();
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            log.warn("Retry [{}] exhausted", name, e);
            return FallbackHandler.handle(joinPoint, retry.fallbackStrategy(), 
                retry.fallbackMethod(), retry.fallbackValue(), e);
        }
    }

    @Around("@annotation(bulkhead)")
    public Object aroundBulkhead(ProceedingJoinPoint joinPoint, Bulkhead bulkhead) throws Throwable {
        String name = getName(joinPoint, bulkhead.name());
        io.github.resilience4j.bulkhead.Bulkhead bulkheadInstance = bulkheadRegistry.bulkhead(name);

        try {
            return bulkheadInstance.executeSupplier(() -> {
                try {
                    return joinPoint.proceed();
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            log.warn("Bulkhead [{}] triggered", name, e);
            return FallbackHandler.handle(joinPoint, bulkhead.fallbackStrategy(), 
                bulkhead.fallbackMethod(), bulkhead.fallbackValue(), e);
        }
    }

    @Around("@annotation(timeLimiter)")
    public Object aroundTimeLimiter(ProceedingJoinPoint joinPoint, TimeLimiter timeLimiter) throws Throwable {
        String name = getName(joinPoint, timeLimiter.name());
        io.github.resilience4j.timelimiter.TimeLimiter limiter = timeLimiterRegistry.timeLimiter(name);

        try {
            return limiter.executeFutureSupplier(() -> 
                java.util.concurrent.CompletableFuture.supplyAsync(() -> {
                    try {
                        return joinPoint.proceed();
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                })
            );
        } catch (Exception e) {
            log.warn("Time limiter [{}] triggered", name, e);
            return FallbackHandler.handle(joinPoint, timeLimiter.fallbackStrategy(), 
                timeLimiter.fallbackMethod(), timeLimiter.fallbackValue(), e);
        }
    }

    private String getName(ProceedingJoinPoint joinPoint, String annotationName) {
        if (StrUtil.isNotBlank(annotationName)) {
            return annotationName;
        }
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getDeclaringType().getSimpleName() + "." + signature.getName();
    }
}
