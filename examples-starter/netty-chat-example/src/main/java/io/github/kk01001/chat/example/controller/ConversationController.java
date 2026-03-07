package io.github.kk01001.chat.example.controller;

import cn.hutool.json.JSONObject;
import io.github.kk01001.chat.example.config.UserContext;
import io.github.kk01001.chat.example.entity.ChatGroup;
import io.github.kk01001.chat.example.entity.ChatUser;
import io.github.kk01001.chat.example.service.ChatConversationService;
import io.github.kk01001.chat.example.service.ChatGroupService;
import io.github.kk01001.chat.example.service.ChatUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author kk01001
 * @date 2026-03-07 19:00:00
 * @description 会话管理接口
 */
@RestController
@RequestMapping("/api/conversation")
@RequiredArgsConstructor
public class ConversationController {

    private final ChatConversationService conversationService;
    private final ChatUserService chatUserService;
    private final ChatGroupService groupService;

    @GetMapping("/list")
    public ResponseEntity<List<JSONObject>> list() {
        Long userId = UserContext.getUserId();
        List<JSONObject> result = conversationService.getUserConversations(userId).stream()
                .map(conv -> {
                    JSONObject json = new JSONObject()
                            .set("convId", conv.getId())
                            .set("targetId", conv.getTargetId())
                            .set("chatType", conv.getChatType())
                            .set("lastMsgContent", conv.getLastMsgContent())
                            .set("lastMsgTime", conv.getLastMsgTime())
                            .set("unreadCount", conv.getUnreadCount());

                    if ("PRIVATE".equals(conv.getChatType())) {
                        ChatUser target = chatUserService.getById(conv.getTargetId());
                        json.set("targetName", target != null ? target.getNickname() : "Unknown");
                        json.set("targetAvatar", target != null ? target.getAvatarFileId() : null);
                    } else {
                        ChatGroup group = groupService.getById(conv.getTargetId());
                        json.set("targetName", group != null ? group.getName() : "Unknown");
                        json.set("targetAvatar", group != null ? group.getAvatarFileId() : null);
                    }
                    return json;
                })
                .toList();
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{convId}/read")
    public ResponseEntity<JSONObject> clearUnread(
            @PathVariable Long convId,
            @RequestBody Map<String, Object> body) {
        Long targetId = Long.valueOf(body.get("targetId").toString());
        String chatType = (String) body.get("chatType");
        conversationService.clearUnread(UserContext.getUserId(), targetId, chatType);
        return ResponseEntity.ok(new JSONObject().set("success", true));
    }

    @DeleteMapping("/{convId}")
    public ResponseEntity<JSONObject> delete(@PathVariable Long convId) {
        conversationService.deleteConversation(convId, UserContext.getUserId());
        return ResponseEntity.ok(new JSONObject().set("success", true));
    }
}
