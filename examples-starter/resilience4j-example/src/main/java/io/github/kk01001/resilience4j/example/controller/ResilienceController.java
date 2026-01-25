package io.github.kk01001.resilience4j.example.controller;

import io.github.kk01001.resilience4j.example.service.RemoteService;
import io.github.kk01001.resilience4j.example.service.ResilienceTestService;
import io.github.kk01001.resilience4j.example.vo.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Resilience4j 测试控制器
 *
 * @author kk01001
 */
@RestController
@RequestMapping("/api/resilience")
public class ResilienceController {

    private final ResilienceTestService testService;
    private final RemoteService remoteService;

    public ResilienceController(ResilienceTestService testService, RemoteService remoteService) {
        this.testService = testService;
        this.remoteService = remoteService;
    }

    /**
     * 测试熔断器
     */
    @GetMapping("/circuit-breaker")
    public ApiResponse<Map<String, Object>> testCircuitBreaker(
            @RequestParam(defaultValue = "50") int failureRate) {
        Map<String, Object> result = new HashMap<>();
        try {
            String response = testService.testCircuitBreaker(failureRate);
            result.put("success", true);
            result.put("response", response);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        result.put("callCount", remoteService.getCallCount());
        return ApiResponse.success(result);
    }

    /**
     * 测试限流器
     */
    @GetMapping("/rate-limiter")
    public ApiResponse<Map<String, Object>> testRateLimiter() {
        Map<String, Object> result = new HashMap<>();
        try {
            String response = testService.testRateLimiter();
            result.put("success", true);
            result.put("response", response);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        result.put("callCount", remoteService.getCallCount());
        return ApiResponse.success(result);
    }

    /**
     * 测试重试
     */
    @GetMapping("/retry")
    public ApiResponse<Map<String, Object>> testRetry(
            @RequestParam(defaultValue = "70") int failureRate) {
        Map<String, Object> result = new HashMap<>();
        try {
            String response = testService.testRetry(failureRate);
            result.put("success", true);
            result.put("response", response);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        result.put("callCount", remoteService.getCallCount());
        return ApiResponse.success(result);
    }

    /**
     * 测试舱壁
     */
    @GetMapping("/bulkhead")
    public ApiResponse<Map<String, Object>> testBulkhead(
            @RequestParam(defaultValue = "1000") long delayMs) {
        Map<String, Object> result = new HashMap<>();
        try {
            String response = testService.testBulkhead(delayMs);
            result.put("success", true);
            result.put("response", response);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return ApiResponse.success(result);
    }

    /**
     * 测试时间限制器
     */
    @GetMapping("/time-limiter")
    public ApiResponse<Map<String, Object>> testTimeLimiter(
            @RequestParam(defaultValue = "3000") long delayMs) {
        Map<String, Object> result = new HashMap<>();
        try {
            String response = testService.testTimeLimiter(delayMs);
            result.put("success", true);
            result.put("response", response);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return ApiResponse.success(result);
    }

    /**
     * 测试组合使用
     */
    @GetMapping("/combined")
    public ApiResponse<Map<String, Object>> testCombined(
            @RequestParam(defaultValue = "30") int failureRate) {
        Map<String, Object> result = new HashMap<>();
        try {
            String response = testService.testCombined(failureRate);
            result.put("success", true);
            result.put("response", response);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        result.put("callCount", remoteService.getCallCount());
        return ApiResponse.success(result);
    }

    /**
     * 重置计数器
     */
    @PostMapping("/reset")
    public ApiResponse<String> reset() {
        remoteService.resetCounter();
        return ApiResponse.success("Counter reset successfully");
    }

    /**
     * 获取统计信息
     */
    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCalls", remoteService.getCallCount());
        return ApiResponse.success(stats);
    }
}
