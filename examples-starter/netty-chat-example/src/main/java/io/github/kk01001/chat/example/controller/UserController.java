package io.github.archer099.chat.example.controller;

import cn.hutool.json.JSONObject;
import io.github.archer099.chat.example.config.UserContext;
import io.github.archer099.chat.example.entity.ChatUser;
import io.github.archer099.chat.example.service.ChatUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author archer099
 * @date 2026-03-07 19:00:00
 * @description 用户接口
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final ChatUserService chatUserService;

    @GetMapping("/info")
    public ResponseEntity<JSONObject> info() {
        ChatUser user = chatUserService.getById(UserContext.getUserId());
        if (user == null) {
            return ResponseEntity.status(404).body(new JSONObject().set("message", "用户不存在"));
        }
        return ResponseEntity.ok(new JSONObject()
                .set("userId", user.getId())
                .set("username", user.getUsername())
                .set("nickname", user.getNickname())
                .set("avatarFileId", user.getAvatarFileId()));
    }

    @GetMapping("/search")
    public ResponseEntity<List<JSONObject>> search(@RequestParam String keyword) {
        List<JSONObject> result = chatUserService.searchUsers(keyword).stream()
                .map(u -> new JSONObject()
                        .set("userId", u.getId())
                        .set("username", u.getUsername())
                        .set("nickname", u.getNickname())
                        .set("avatarFileId", u.getAvatarFileId()))
                .toList();
        return ResponseEntity.ok(result);
    }

    @PutMapping("/profile")
    public ResponseEntity<JSONObject> updateProfile(@RequestBody Map<String, Object> body) {
        ChatUser user = chatUserService.getById(UserContext.getUserId());
        if (body.containsKey("nickname")) {
            user.setNickname((String) body.get("nickname"));
        }
        if (body.containsKey("avatarFileId")) {
            user.setAvatarFileId(Long.valueOf(body.get("avatarFileId").toString()));
        }
        chatUserService.updateById(user);
        return ResponseEntity.ok(new JSONObject().set("success", true));
    }
}
