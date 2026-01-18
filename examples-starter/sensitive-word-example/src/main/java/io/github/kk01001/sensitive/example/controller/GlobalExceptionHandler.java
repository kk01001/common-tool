package io.github.kk01001.sensitive.example.controller;

import io.github.kk01001.sensitive.core.SensitiveWordException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * @author kk01001
 * @date 2026-01-18 13:02:31
 * @description 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理敏感词异常
     */
    @ExceptionHandler(SensitiveWordException.class)
    public ResponseEntity<Map<String, Object>> handleSensitiveWordException(SensitiveWordException e) {
        log.warn("检测到敏感词: {}", e.getAllSensitiveWords());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "success", false,
                        "code", 400,
                        "message", e.getMessage(),
                        "sensitiveWords", e.getAllSensitiveWords()
                ));
    }

    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception e) {
        log.error("系统异常", e);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "success", false,
                        "code", 500,
                        "message", "系统错误: " + e.getMessage()
                ));
    }
}
