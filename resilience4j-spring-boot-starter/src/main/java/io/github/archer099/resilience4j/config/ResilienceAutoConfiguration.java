package io.github.archer099.resilience4j.config;

import io.github.archer099.resilience4j.aspect.ResilienceAspect;
import io.github.archer099.resilience4j.properties.ResilienceProperties;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Resilience4j 自动配置
 *
 * @author archer099
 */
@Configuration
@EnableConfigurationProperties(ResilienceProperties.class)
@ConditionalOnProperty(prefix = "resilience4j", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ResilienceAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ResilienceAutoConfiguration.class);

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        log.info("Initializing CircuitBreakerRegistry");
        return CircuitBreakerRegistry.ofDefaults();
    }

    @Bean
    public RateLimiterRegistry rateLimiterRegistry() {
        log.info("Initializing RateLimiterRegistry");
        return RateLimiterRegistry.ofDefaults();
    }

    @Bean
    public RetryRegistry retryRegistry() {
        log.info("Initializing RetryRegistry");
        return RetryRegistry.ofDefaults();
    }

    @Bean
    public BulkheadRegistry bulkheadRegistry() {
        log.info("Initializing BulkheadRegistry");
        return BulkheadRegistry.ofDefaults();
    }

    @Bean
    public TimeLimiterRegistry timeLimiterRegistry() {
        log.info("Initializing TimeLimiterRegistry");
        return TimeLimiterRegistry.ofDefaults();
    }

    @Bean
    public ResilienceAspect resilienceAspect(CircuitBreakerRegistry circuitBreakerRegistry,
                                            RateLimiterRegistry rateLimiterRegistry,
                                            RetryRegistry retryRegistry,
                                            BulkheadRegistry bulkheadRegistry,
                                            TimeLimiterRegistry timeLimiterRegistry) {
        log.info("Initializing ResilienceAspect");
        return new ResilienceAspect(circuitBreakerRegistry, rateLimiterRegistry, 
            retryRegistry, bulkheadRegistry, timeLimiterRegistry);
    }
}
