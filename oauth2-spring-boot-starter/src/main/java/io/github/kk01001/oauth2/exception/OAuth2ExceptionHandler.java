package io.github.kk01001.oauth2.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2TokenRevocationAuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * @author kk01001
 * @date 2025-02-22 14:31:00
 * @description OAuth2异常处理器
 */
@RestControllerAdvice
public class OAuth2ExceptionHandler {

    /**
     * 处理OAuth2认证异常
     */
    @ExceptionHandler(OAuth2AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleOAuth2Exception(OAuth2AuthenticationException ex) {
        Map<String, Object> result = new HashMap<>();
        OAuth2Error error = ex.getError();
        result.put("error", error.getErrorCode());
        result.put("error_description", error.getDescription());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
    }

    /**
     * 处理令牌撤销异常
     */
    @ExceptionHandler(OAuth2TokenRevocationAuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleTokenRevocationException(
            OAuth2TokenRevocationAuthenticationException ex) {
        Map<String, Object> result = new HashMap<>();
        result.put("error", "token_revocation_error");
        result.put("error_description", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }

    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception ex) {
        Map<String, Object> result = new HashMap<>();
        result.put("error", "server_error");
        result.put("error_description", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }
} 