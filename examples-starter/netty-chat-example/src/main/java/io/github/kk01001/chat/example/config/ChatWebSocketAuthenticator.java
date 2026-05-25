package io.github.archer099.chat.example.config;

import io.github.archer099.netty.auth.WebSocketAuthenticator;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author archer099
 * @date 2026-03-07 18:00:00
 * @description WebSocket JWT 鉴权实现
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketAuthenticator implements WebSocketAuthenticator {

    private final JwtUtil jwtUtil;

    @Override
    public AuthResult authenticate(FullHttpRequest request) {
        QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
        List<String> tokens = decoder.parameters().get("token");

        if (tokens == null || tokens.isEmpty()) {
            return AuthResult.fail("缺少 token 参数");
        }

        String token = tokens.getFirst();
        if (!jwtUtil.isValid(token)) {
            return AuthResult.fail("token 无效或已过期");
        }

        Long userId = jwtUtil.getUserId(token);
        if (userId == null) {
            return AuthResult.fail("token 解析失败");
        }

        log.debug("WebSocket JWT 鉴权通过: userId={}", userId);
        return AuthResult.success(String.valueOf(userId));
    }
}
