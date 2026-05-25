package io.github.archer099.chat.example.controller;

import cn.hutool.json.JSONObject;
import io.github.archer099.chat.example.config.UserContext;
import io.github.archer099.chat.example.entity.ChatUser;
import io.github.archer099.chat.example.service.ChatFriendshipService;
import io.github.archer099.chat.example.service.ChatUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author archer099
 * @date 2026-03-07 19:00:00
 * @description 好友管理接口
 */
@RestController
@RequestMapping("/api/friend")
@RequiredArgsConstructor
public class FriendController {

    private final ChatFriendshipService friendshipService;
    private final ChatUserService chatUserService;

    @PostMapping("/request")
    public ResponseEntity<JSONObject> sendRequest(@RequestBody Map<String, Object> body) {
        Long userId = UserContext.getUserId();
        Long friendId = Long.valueOf(body.get("friendId").toString());
        String requestMsg = (String) body.getOrDefault("requestMsg", "");

        if (userId.equals(friendId)) {
            return ResponseEntity.ok(new JSONObject().set("success", false).set("message", "不能添加自己"));
        }
        if (friendshipService.isFriend(userId, friendId)) {
            return ResponseEntity.ok(new JSONObject().set("success", false).set("message", "已经是好友"));
        }

        var fs = friendshipService.sendRequest(userId, friendId, requestMsg);
        return ResponseEntity.ok(new JSONObject().set("success", true).set("requestId", fs.getId()));
    }

    @GetMapping("/requests")
    public ResponseEntity<List<JSONObject>> getRequests() {
        Long userId = UserContext.getUserId();
        List<JSONObject> result = friendshipService.getPendingRequests(userId).stream()
                .map(fs -> {
                    ChatUser requester = chatUserService.getById(fs.getUserId());
                    return new JSONObject()
                            .set("requestId", fs.getId())
                            .set("userId", fs.getUserId())
                            .set("nickname", requester != null ? requester.getNickname() : "Unknown")
                            .set("username", requester != null ? requester.getUsername() : "")
                            .set("avatarFileId", requester != null ? requester.getAvatarFileId() : null)
                            .set("requestMsg", fs.getRequestMsg())
                            .set("createTime", fs.getCreateTime());
                })
                .toList();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/accept/{requestId}")
    public ResponseEntity<JSONObject> accept(@PathVariable Long requestId) {
        boolean ok = friendshipService.acceptRequest(requestId, UserContext.getUserId());
        return ResponseEntity.ok(new JSONObject().set("success", ok));
    }

    @PostMapping("/reject/{requestId}")
    public ResponseEntity<JSONObject> reject(@PathVariable Long requestId) {
        boolean ok = friendshipService.rejectRequest(requestId, UserContext.getUserId());
        return ResponseEntity.ok(new JSONObject().set("success", ok));
    }

    @GetMapping("/list")
    public ResponseEntity<List<JSONObject>> friendList() {
        List<JSONObject> result = friendshipService.getFriendList(UserContext.getUserId()).stream()
                .map(fs -> {
                    ChatUser friend = chatUserService.getById(fs.getFriendId());
                    return new JSONObject()
                            .set("friendId", fs.getFriendId())
                            .set("nickname", friend != null ? friend.getNickname() : "Unknown")
                            .set("username", friend != null ? friend.getUsername() : "")
                            .set("avatarFileId", friend != null ? friend.getAvatarFileId() : null);
                })
                .toList();
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{friendId}")
    public ResponseEntity<JSONObject> deleteFriend(@PathVariable Long friendId) {
        friendshipService.deleteFriend(UserContext.getUserId(), friendId);
        return ResponseEntity.ok(new JSONObject().set("success", true));
    }
}
