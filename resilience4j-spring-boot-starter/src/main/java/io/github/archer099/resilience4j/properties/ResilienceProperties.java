package io.github.archer099.resilience4j.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Resilience4j 配置属性
 *
 * @author archer099
 */
@ConfigurationProperties(prefix = "resilience4j")
public class ResilienceProperties {

    private boolean enabled = true;
    private CircuitBreakerConfig circuitBreaker = new CircuitBreakerConfig();
    private RateLimiterConfig rateLimiter = new RateLimiterConfig();
    private RetryConfig retry = new RetryConfig();
    private BulkheadConfig bulkhead = new BulkheadConfig();
    private TimeLimiterConfig timeLimiter = new TimeLimiterConfig();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public CircuitBreakerConfig getCircuitBreaker() {
        return circuitBreaker;
    }

    public void setCircuitBreaker(CircuitBreakerConfig circuitBreaker) {
        this.circuitBreaker = circuitBreaker;
    }

    public RateLimiterConfig getRateLimiter() {
        return rateLimiter;
    }

    public void setRateLimiter(RateLimiterConfig rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    public RetryConfig getRetry() {
        return retry;
    }

    public void setRetry(RetryConfig retry) {
        this.retry = retry;
    }

    public BulkheadConfig getBulkhead() {
        return bulkhead;
    }

    public void setBulkhead(BulkheadConfig bulkhead) {
        this.bulkhead = bulkhead;
    }

    public TimeLimiterConfig getTimeLimiter() {
        return timeLimiter;
    }

    public void setTimeLimiter(TimeLimiterConfig timeLimiter) {
        this.timeLimiter = timeLimiter;
    }

    public static class CircuitBreakerConfig {
        private Map<String, InstanceConfig> instances = new HashMap<>();

        public Map<String, InstanceConfig> getInstances() {
            return instances;
        }

        public void setInstances(Map<String, InstanceConfig> instances) {
            this.instances = instances;
        }

        public static class InstanceConfig {
            private Float failureRateThreshold = 50.0f;
            private Float slowCallRateThreshold = 100.0f;
            private Long slowCallDurationThreshold = 60000L;
            private Integer slidingWindowSize = 100;
            private Integer minimumNumberOfCalls = 10;

            public Float getFailureRateThreshold() {
                return failureRateThreshold;
            }

            public void setFailureRateThreshold(Float failureRateThreshold) {
                this.failureRateThreshold = failureRateThreshold;
            }

            public Float getSlowCallRateThreshold() {
                return slowCallRateThreshold;
            }

            public void setSlowCallRateThreshold(Float slowCallRateThreshold) {
                this.slowCallRateThreshold = slowCallRateThreshold;
            }

            public Long getSlowCallDurationThreshold() {
                return slowCallDurationThreshold;
            }

            public void setSlowCallDurationThreshold(Long slowCallDurationThreshold) {
                this.slowCallDurationThreshold = slowCallDurationThreshold;
            }

            public Integer getSlidingWindowSize() {
                return slidingWindowSize;
            }

            public void setSlidingWindowSize(Integer slidingWindowSize) {
                this.slidingWindowSize = slidingWindowSize;
            }

            public Integer getMinimumNumberOfCalls() {
                return minimumNumberOfCalls;
            }

            public void setMinimumNumberOfCalls(Integer minimumNumberOfCalls) {
                this.minimumNumberOfCalls = minimumNumberOfCalls;
            }
        }
    }

    public static class RateLimiterConfig {
        private Map<String, InstanceConfig> instances = new HashMap<>();

        public Map<String, InstanceConfig> getInstances() {
            return instances;
        }

        public void setInstances(Map<String, InstanceConfig> instances) {
            this.instances = instances;
        }

        public static class InstanceConfig {
            private Integer limitForPeriod = 50;
            private Long limitRefreshPeriod = 500_000_000L;
            private Long timeoutDuration = 5000L;

            public Integer getLimitForPeriod() {
                return limitForPeriod;
            }

            public void setLimitForPeriod(Integer limitForPeriod) {
                this.limitForPeriod = limitForPeriod;
            }

            public Long getLimitRefreshPeriod() {
                return limitRefreshPeriod;
            }

            public void setLimitRefreshPeriod(Long limitRefreshPeriod) {
                this.limitRefreshPeriod = limitRefreshPeriod;
            }

            public Long getTimeoutDuration() {
                return timeoutDuration;
            }

            public void setTimeoutDuration(Long timeoutDuration) {
                this.timeoutDuration = timeoutDuration;
            }
        }
    }

    public static class RetryConfig {
        private Map<String, InstanceConfig> instances = new HashMap<>();

        public Map<String, InstanceConfig> getInstances() {
            return instances;
        }

        public void setInstances(Map<String, InstanceConfig> instances) {
            this.instances = instances;
        }

        public static class InstanceConfig {
            private Integer maxAttempts = 3;
            private Long waitDuration = 500L;

            public Integer getMaxAttempts() {
                return maxAttempts;
            }

            public void setMaxAttempts(Integer maxAttempts) {
                this.maxAttempts = maxAttempts;
            }

            public Long getWaitDuration() {
                return waitDuration;
            }

            public void setWaitDuration(Long waitDuration) {
                this.waitDuration = waitDuration;
            }
        }
    }

    public static class BulkheadConfig {
        private Map<String, InstanceConfig> instances = new HashMap<>();

        public Map<String, InstanceConfig> getInstances() {
            return instances;
        }

        public void setInstances(Map<String, InstanceConfig> instances) {
            this.instances = instances;
        }

        public static class InstanceConfig {
            private Integer maxConcurrentCalls = 25;
            private Long maxWaitDuration = 0L;

            public Integer getMaxConcurrentCalls() {
                return maxConcurrentCalls;
            }

            public void setMaxConcurrentCalls(Integer maxConcurrentCalls) {
                this.maxConcurrentCalls = maxConcurrentCalls;
            }

            public Long getMaxWaitDuration() {
                return maxWaitDuration;
            }

            public void setMaxWaitDuration(Long maxWaitDuration) {
                this.maxWaitDuration = maxWaitDuration;
            }
        }
    }

    public static class TimeLimiterConfig {
        private Map<String, InstanceConfig> instances = new HashMap<>();

        public Map<String, InstanceConfig> getInstances() {
            return instances;
        }

        public void setInstances(Map<String, InstanceConfig> instances) {
            this.instances = instances;
        }

        public static class InstanceConfig {
            private Long timeoutDuration = 1000L;
            private Boolean cancelRunningFuture = true;

            public Long getTimeoutDuration() {
                return timeoutDuration;
            }

            public void setTimeoutDuration(Long timeoutDuration) {
                this.timeoutDuration = timeoutDuration;
            }

            public Boolean getCancelRunningFuture() {
                return cancelRunningFuture;
            }

            public void setCancelRunningFuture(Boolean cancelRunningFuture) {
                this.cancelRunningFuture = cancelRunningFuture;
            }
        }
    }
}
