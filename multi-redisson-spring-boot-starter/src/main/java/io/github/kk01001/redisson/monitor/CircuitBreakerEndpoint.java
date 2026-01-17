package io.github.kk01001.redisson.monitor;

import io.github.kk01001.redisson.circuitbreaker.DualWriteCircuitBreaker;
import io.github.kk01001.redisson.properties.MultiRedissonProperties;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author kk01001
 * @date 2026-01-15 19:00:00
 * @description 熔断器操作端点
 * <p>
 * 访问路径：/actuator/redissoncircuitbreaker
 * </p>
 */
@Endpoint(id = "redissoncircuitbreaker")
public class CircuitBreakerEndpoint {

    private final DualWriteCircuitBreaker circuitBreaker;

    public CircuitBreakerEndpoint(DualWriteCircuitBreaker circuitBreaker) {
        this.circuitBreaker = circuitBreaker;
    }

    /**
     * 获取熔断器状态和配置
     * GET /actuator/redissoncircuitbreaker
     */
    @ReadOperation
    public Map<String, Object> info() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        // 状态
        DualWriteCircuitBreaker.CircuitBreakerStats stats = circuitBreaker.getStats();
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("state", stats.state().name());
        status.put("totalCalls", stats.totalCalls());
        status.put("successCalls", stats.successCalls());
        status.put("failureCalls", stats.failureCalls());
        status.put("failureRate", String.format("%.2f%%", stats.failureRate()));
        if (stats.openTimestamp() > 0) {
            status.put("openTime", LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(stats.openTimestamp()), ZoneId.systemDefault())
                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        result.put("status", status);

        // 配置（直接读取 Properties，支持 Nacos 动态刷新）
        MultiRedissonProperties.CircuitBreaker config = circuitBreaker.getConfig();
        result.put("config", buildConfigMap(config));

        return result;
    }

    /**
     * 操作熔断器
     * POST /actuator/redissoncircuitbreaker/{action}
     *
     * @param action open | close | reset
     */
    @WriteOperation
    public Map<String, Object> operate(@Selector String action) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        result.put("action", action);

        switch (action.toLowerCase()) {
            case "open" -> {
                circuitBreaker.forceOpen();
                result.put("success", true);
                result.put("message", "Circuit breaker forced open");
                result.put("currentState", circuitBreaker.getState().name());
            }
            case "close" -> {
                circuitBreaker.forceClose();
                result.put("success", true);
                result.put("message", "Circuit breaker forced close");
                result.put("currentState", circuitBreaker.getState().name());
            }
            case "reset" -> {
                circuitBreaker.reset();
                result.put("success", true);
                result.put("message", "Circuit breaker statistics reset");
                result.put("currentState", circuitBreaker.getState().name());
            }
            default -> {
                result.put("success", false);
                result.put("message", "Unknown action: " + action);
                result.put("availableActions", new String[]{"open", "close", "reset"});
            }
        }

        return result;
    }

    private Map<String, Object> buildConfigMap(MultiRedissonProperties.CircuitBreaker config) {
        Map<String, Object> configMap = new LinkedHashMap<>();
        configMap.put("enabled", config.isEnabled());
        configMap.put("failureRateThreshold", config.getFailureRateThreshold());
        configMap.put("slidingWindowSize", config.getSlidingWindowSize());
        configMap.put("minimumNumberOfCalls", config.getMinimumNumberOfCalls());
        configMap.put("waitDurationInOpenState", config.getWaitDurationInOpenState());
        configMap.put("permittedCallsInHalfOpenState", config.getPermittedCallsInHalfOpenState());
        return configMap;
    }
}
