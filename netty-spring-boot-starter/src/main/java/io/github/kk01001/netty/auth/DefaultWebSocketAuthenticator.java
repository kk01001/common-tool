package io.github.kk01001.netty.auth;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.springframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 默认的WebSocket鉴权实现
 * 从请求参数中获取token进行验证
 */
public class DefaultWebSocketAuthenticator implements WebSocketAuthenticator {
    
    private static final Logger log = LoggerFactory.getLogger(DefaultWebSocketAuthenticator.class);
    
    @Override
    public AuthResult authenticate(FullHttpRequest request) {
        // 获取完整的URI，包括查询参数
        String fullUri = request.uri();
        log.debug("开始解析WebSocket请求: fullUri={}", fullUri);
        
        QueryStringDecoder decoder = new QueryStringDecoder(fullUri);
        
        // 打印调试信息
        log.debug("收到WebSocket连接请求: uri={}, parameters={}", 
            fullUri, decoder.parameters());
        
        String token = decoder.parameters().getOrDefault("token", java.util.List.of("")).get(0);
        log.debug("解析到token: {}", token);
        
        if (!StringUtils.hasText(token)) {
            log.warn("token为空");
            return AuthResult.fail("Missing token");
        }
        
        // 默认实现：token即为userId
        log.debug("鉴权成功: token={}", token);
        return AuthResult.success(token);
    }
} 