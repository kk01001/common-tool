package io.github.kk01001.oauth2.example.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author kk01001
 * @date 2025-02-22 14:31:00
 * @description 资源控制器
 */
@RestController
@RequestMapping("/api")
public class ResourceController {

    @GetMapping("/resource")
    public Map<String, Object> getResource(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> resource = new HashMap<>();
        resource.put("message", "Protected Resource");
        resource.put("user", jwt.getSubject());
        resource.put("scopes", jwt.getClaimAsString("scope"));
        return resource;
    }

    @GetMapping("/public/resource")
    public Map<String, Object> getPublicResource() {
        Map<String, Object> resource = new HashMap<>();
        resource.put("message", "Public Resource");
        return resource;
    }
} 