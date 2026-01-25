package io.github.kk01001.resilience4j.example.service;

import io.github.kk01001.resilience4j.annotation.*;
import io.github.kk01001.resilience4j.enums.FallbackStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Resilience4j 测试服务
 *
 * @author kk01001
 */
@Service
public class ResilienceTestService {

    private static final Logger log = LoggerFactory.getLogger(ResilienceTestService.class);
    private final RemoteService remoteService;

    public ResilienceTestService(RemoteService remoteService) {
        this.remoteService = remoteService;
    }

    // ==================== 熔断器测试 ====================

    @CircuitBreaker(
        name = "testCircuitBreaker",
        failureRateThreshold = 50.0f,
        fallbackStrategy = FallbackStrategy.METHOD,
        fallbackMethod = "circuitBreakerFallback"
    )
    public String testCircuitBreaker(int failureRate) {
        return remoteService.unstableCall(failureRate);
    }

    public String circuitBreakerFallback(int failureRate, Throwable throwable) {
        log.warn("Circuit breaker fallback triggered", throwable);
        return "Fallback: Service temporarily unavailable";
    }

    // ==================== 限流器测试 ====================

    @RateLimiter(
        name = "testRateLimiter",
        limitForPeriod = 5,
        limitRefreshPeriod = 1000_000_000,
        fallbackStrategy = FallbackStrategy.METHOD,
        fallbackMethod = "rateLimiterFallback"
    )
    public String testRateLimiter() {
        return remoteService.normalCall();
    }

    public String rateLimiterFallback(Throwable throwable) {
        log.warn("Rate limiter fallback triggered", throwable);
        return "Fallback: Too many requests, please try again later";
    }

    // ==================== 重试测试 ====================

    @Retry(
        name = "testRetry",
        maxAttempts = 3,
        waitDuration = 500,
        fallbackStrategy = FallbackStrategy.METHOD,
        fallbackMethod = "retryFallback"
    )
    public String testRetry(int failureRate) {
        log.info("Attempting call...");
        return remoteService.unstableCall(failureRate);
    }

    public String retryFallback(int failureRate, Throwable throwable) {
        log.warn("Retry exhausted, using fallback", throwable);
        return "Fallback: All retry attempts failed";
    }

    // ==================== 舱壁测试 ====================

    @Bulkhead(
        name = "testBulkhead",
        maxConcurrentCalls = 3,
        type = Bulkhead.Type.SEMAPHORE,
        fallbackStrategy = FallbackStrategy.METHOD,
        fallbackMethod = "bulkheadFallback"
    )
    public String testBulkhead(long delayMs) {
        return remoteService.slowCall(delayMs);
    }

    public String bulkheadFallback(long delayMs, Throwable throwable) {
        log.warn("Bulkhead fallback triggered", throwable);
        return "Fallback: Too many concurrent requests";
    }

    // ==================== 时间限制器测试 ====================

    @TimeLimiter(
        name = "testTimeLimiter",
        timeoutDuration = 2000,
        fallbackStrategy = FallbackStrategy.METHOD,
        fallbackMethod = "timeLimiterFallback"
    )
    public String testTimeLimiter(long delayMs) {
        return remoteService.slowCall(delayMs);
    }

    public String timeLimiterFallback(long delayMs, Throwable throwable) {
        log.warn("Time limiter fallback triggered", throwable);
        return "Fallback: Request timeout";
    }

    // ==================== 组合使用测试 ====================

    @Retry(name = "combined", maxAttempts = 2)
    @CircuitBreaker(name = "combined")
    @RateLimiter(name = "combined", limitForPeriod = 10)
    public String testCombined(int failureRate) {
        return remoteService.unstableCall(failureRate);
    }
}
