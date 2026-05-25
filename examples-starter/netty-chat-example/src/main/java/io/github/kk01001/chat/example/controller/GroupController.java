package io.github.archer099.chat.example.controller;

import cn.hutool.json.JSONObject;
import io.github.archer099.chat.example.config.UserContext;
import io.github.archer099.chat.example.entity.ChatGroup;
import io.github.archer099.chat.example.entity.ChatUser;
import io.github.archer099.chat.example.service.ChatGroupService;
import io.github.archer099.chat.example.service.ChatUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author archer099
 * @date 2026-03-07 19:00:00
 * @description 群组管理接口
 */
@RestController
@RequestMapping("/api/group")
@RequiredArgsConstructor
public class GroupController {

    private final ChatGroupService groupService;
    private final ChatUserService chatUserService;

    @PostMapping("/create")
    public ResponseEntity<JSONObject> create(@RequestBody Map<String, String> body) {
        ChatGroup group = groupService.createGroup(body.get("name"), UserContext.getUserId());
        return ResponseEntity.ok(new JSONObject()
                .set("success", true)
                .set("groupId", group.getId())
                .set("name", group.getName()));
    }

    @PostMapping("/{groupId}/invite")
    public ResponseEntity<JSONObject> invite(@PathVariable Long groupId, @RequestBody Map<String, Object> body) {
        if (!groupService.isMember(groupId, UserContext.getUserId())) {
            return ResponseEntity.ok(new JSONObject().set("success", false).set("message", "你不是群成员"));
        }
        Long inviteeId = Long.valueOf(body.get("userId").toString());
        boolean ok = groupService.addMember(groupId, inviteeId);
        return ResponseEntity.ok(new JSONObject()
                .set("success", ok)
                .set("message", ok ? "邀请成功" : "该用户已在群中"));
    }

    @PostMapping("/{groupId}/leave")
    public ResponseEntity<JSONObject> leave(@PathVariable Long groupId) {
        Long userId = UserContext.getUserId();
        ChatGroup group = groupService.getById(groupId);
        if (group != null && group.getOwnerId().equals(userId)) {
            return ResponseEntity.ok(new JSONObject().set("success", false).set("message", "群主不能退出，请先转让或解散"));
        }
        groupService.removeMember(groupId, userId);
        return ResponseEntity.ok(new JSONObject().set("success", true));
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<JSONObject> dissolve(@PathVariable Long groupId) {
        boolean ok = groupService.dissolveGroup(groupId, UserContext.getUserId());
        return ResponseEntity.ok(new JSONObject().set("success", ok));
    }

    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<JSONObject>> members(@PathVariable Long groupId) {
        List<JSONObject> result = groupService.getMembers(groupId).stream()
                .map(m -> {
                    ChatUser user = chatUserService.getById(m.getUserId());
                    return new JSONObject()
                            .set("userId", m.getUserId())
                            .set("role", m.getRole())
                            .set("nickname", user != null ? user.getNickname() : "Unknown")
                            .set("username", user != null ? user.getUsername() : "")
                            .set("joinTime", m.getJoinTime());
                })
                .toList();
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{groupId}/notice")
    public ResponseEntity<JSONObject> updateNotice(@PathVariable Long groupId, @RequestBody Map<String, String> body) {
        ChatGroup group = groupService.getById(groupId);
        if (group == null || !group.getOwnerId().equals(UserContext.getUserId())) {
            return ResponseEntity.ok(new JSONObject().set("success", false).set("message", "无权限"));
        }
        group.setNotice(body.get("notice"));
        groupService.updateById(group);
        return ResponseEntity.ok(new JSONObject().set("success", true));
    }

    @GetMapping("/list")
    public ResponseEntity<List<JSONObject>> myGroups() {
        List<JSONObject> result = groupService.getUserGroups(UserContext.getUserId()).stream()
                .map(g -> new JSONObject()
                        .set("groupId", g.getId())
                        .set("name", g.getName())
                        .set("ownerId", g.getOwnerId())
                        .set("notice", g.getNotice()))
                .toList();
        return ResponseEntity.ok(result);
    }
}
