package io.github.archer099.chat.example.controller;

import cn.hutool.json.JSONObject;
import io.github.archer099.chat.example.config.JwtUtil;
import io.github.archer099.chat.example.entity.ChatUser;
import io.github.archer099.chat.example.service.ChatUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author archer099
 * @date 2026-03-07 18:00:00
 * @description 登录注册接口
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final ChatUserService chatUserService;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<JSONObject> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        ChatUser user = chatUserService.login(username, password);
        if (user == null) {
            return ResponseEntity.ok(new JSONObject()
                    .set("success", false)
                    .set("message", "用户名或密码错误"));
        }

        String token = jwtUtil.generateToken(user.getId(), user.getNickname());
        return ResponseEntity.ok(new JSONObject()
                .set("success", true)
                .set("token", token)
                .set("userId", user.getId())
                .set("nickname", user.getNickname())
                .set("username", user.getUsername())
                .set("avatarFileId", user.getAvatarFileId()));
    }

    @PostMapping("/register")
    public ResponseEntity<JSONObject> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        String nickname = body.get("nickname");

        if (username == null || password == null || nickname == null) {
            return ResponseEntity.ok(new JSONObject()
                    .set("success", false)
                    .set("message", "参数不完整"));
        }

        ChatUser user = chatUserService.register(username, password, nickname);
        if (user == null) {
            return ResponseEntity.ok(new JSONObject()
                    .set("success", false)
                    .set("message", "用户名已存在"));
        }

        String token = jwtUtil.generateToken(user.getId(), user.getNickname());
        return ResponseEntity.ok(new JSONObject()
                .set("success", true)
                .set("token", token)
                .set("userId", user.getId())
                .set("nickname", user.getNickname())
                .set("username", user.getUsername()));
    }
}
