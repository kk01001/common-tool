package io.github.kk01001.netty.auth;

import io.netty.handler.codec.http.FullHttpRequest;

/**
 * WebSocket鉴权接口
 */
public interface WebSocketAuthenticator {
    
    /**
     * 鉴权
     * @param request HTTP请求
     * @return 鉴权结果
     */
    AuthResult authenticate(FullHttpRequest request);
    
    /**
     * 鉴权结果
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    class AuthResult {
        /**
         * 是否通过鉴权
         */
        private boolean success;
        
        /**
         * 鉴权失败原因
         */
        private String message;
        
        /**
         * 用户标识（可选）
         */
        private String userId;
        
        public static AuthResult success(String userId) {
            return new AuthResult(true, null, userId);
        }
        
        public static AuthResult fail(String message) {
            return new AuthResult(false, message, null);
        }
    }
} 