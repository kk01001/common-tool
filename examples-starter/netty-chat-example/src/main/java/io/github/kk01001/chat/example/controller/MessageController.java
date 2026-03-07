package io.github.kk01001.chat.example.controller;

import cn.hutool.json.JSONObject;
import io.github.kk01001.chat.example.config.UserContext;
import io.github.kk01001.chat.example.entity.ChatMessage;
import io.github.kk01001.chat.example.entity.ChatUser;
import io.github.kk01001.chat.example.service.ChatMessageService;
import io.github.kk01001.chat.example.service.ChatUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author kk01001
 * @date 2026-03-07 19:00:00
 * @description 消息查询接口
 */
@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor
public class MessageController {

    private final ChatMessageService chatMessageService;
    private final ChatUserService chatUserService;

    @GetMapping("/private/history")
    public ResponseEntity<List<JSONObject>> privateHistory(
            @RequestParam Long targetId,
            @RequestParam(required = false) Long beforeId,
            @RequestParam(defaultValue = "50") int limit) {
        List<ChatMessage> messages = chatMessageService.getPrivateHistory(
                UserContext.getUserId(), targetId, beforeId, limit);
        return ResponseEntity.ok(buildMessageList(messages));
    }

    @GetMapping("/group/history")
    public ResponseEntity<List<JSONObject>> groupHistory(
            @RequestParam Long groupId,
            @RequestParam(required = false) Long beforeId,
            @RequestParam(defaultValue = "50") int limit) {
        List<ChatMessage> messages = chatMessageService.getGroupHistory(groupId, beforeId, limit);
        return ResponseEntity.ok(buildMessageList(messages));
    }

    private List<JSONObject> buildMessageList(List<ChatMessage> messages) {
        return messages.reversed().stream().map(msg -> {
            ChatUser sender = chatUserService.getById(msg.getSenderId());
            return new JSONObject()
                    .set("messageId", msg.getId())
                    .set("senderId", msg.getSenderId())
                    .set("nickname", sender != null ? sender.getNickname() : "Unknown")
                    .set("chatType", msg.getChatType())
                    .set("type", msg.getMsgType())
                    .set("content", msg.getContent())
                    .set("fileId", msg.getFileId())
                    .set("groupId", msg.getGroupId())
                    .set("status", msg.getStatus())
                    .set("timestamp", msg.getCreateTime());
        }).toList();
    }
}
