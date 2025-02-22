package io.github.kk01001.oauth2.endpoint;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author kk01001
 * @date 2025-02-22 14:31:00
 * @description 用户信息端点
 */
@RestController
@RequestMapping("/oauth2")
public class UserInfoEndpoint {

    /**
     * 获取用户信息
     */
    @GetMapping("/userinfo")
    public Map<String, Object> getUserInfo(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("sub", jwt.getSubject());
        userInfo.put("name", jwt.getClaim("name"));
        userInfo.put("email", jwt.getClaim("email"));
        userInfo.put("scope", jwt.getClaim("scope"));
        return userInfo;
    }
} 