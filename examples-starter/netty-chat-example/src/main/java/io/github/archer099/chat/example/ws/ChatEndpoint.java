package io.github.archer099.chat.example.ws;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import io.github.archer099.chat.example.entity.*;
import io.github.archer099.chat.example.service.*;
import io.github.archer099.netty.annotation.*;
import io.github.archer099.netty.session.WebSocketSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.List;

/**
 * @author archer099
 * @date 2026-03-07 18:00:00
 * @description IM 聊天 WebSocket 端点，处理单聊/群聊/好友/已读回执
 */
@Slf4j
@Component
@RequiredArgsConstructor
@WebSocketEndpoint("/ws/chat")
public class ChatEndpoint {

    private final ChatUserService chatUserService;
    private final ChatMessageService chatMessageService;
    private final ChatFileService chatFileService;
    private final ChatFriendshipService friendshipService;
    private final ChatGroupService groupService;
    private final ChatConversationService conversationService;
    private final OnlineUserManager onlineUserManager;

    @OnOpen
    public void onOpen(WebSocketSession session) {
        String userId = session.getUserId();
        if (userId == null) {
            return;
        }

        ChatUser user = chatUserService.getById(Long.valueOf(userId));
        if (user == null) {
            return;
        }

        session.setAttribute("nickname", user.getNickname());
        session.setAttribute("userId", userId);
        onlineUserManager.addUser(userId, session);

        log.info("用户上线: userId={}, nickname={}, 在线: {}", userId, user.getNickname(), onlineUserManager.getOnlineCount());

        pushOfflineMessages(session, userId);
        pushOnlineStatus(userId, true);
    }

    @OnMessage
    public void onMessage(WebSocketSession session, String message) {
        String userId = session.getUserId();
        if (userId == null) {
            return;
        }

        try {
            JSONObject msgJson = JSONUtil.parseObj(message);
            String action = msgJson.getStr("action", "CHAT_MSG");

            switch (action) {
                case "CHAT_MSG" -> handleChatMessage(session, msgJson, userId);
                case "READ_RECEIPT" -> handleReadReceipt(msgJson, userId);
                case "FRIEND_REQUEST" -> handleFriendRequest(msgJson, userId);
                case "FRIEND_ACCEPT" -> handleFriendAccept(msgJson, userId);
                default -> log.warn("未知 action: {}", action);
            }
        } catch (Exception e) {
            log.error("处理消息异常: userId={}", userId, e);
            session.sendMessage(new JSONObject()
                    .set("action", "ERROR")
                    .set("content", "消息处理失败: " + e.getMessage())
                    .toString());
        }
    }

    @OnBinaryMessage
    public void onBinaryMessage(WebSocketSession session, byte[] data) {
        String userId = session.getUserId();
        if (userId == null) {
            return;
        }

        try {
            ChatFile chatFile = chatFileService.storeFromBytes(
                    data, "binary_" + System.currentTimeMillis(),
                    "application/octet-stream", Long.valueOf(userId));

            session.sendMessage(new JSONObject()
                    .set("action", "FILE_UPLOADED")
                    .set("fileId", chatFile.getId())
                    .set("fileName", chatFile.getFileName())
                    .set("fileSize", chatFile.getFileSize())
                    .toString());
        } catch (Exception e) {
            log.error("处理二进制消息异常: userId={}", userId, e);
        }
    }

    @OnClose
    public void onClose(WebSocketSession session) {
        String userId = session.getUserId();
        if (userId != null) {
            onlineUserManager.removeUser(userId);
            pushOnlineStatus(userId, false);
            log.info("用户下线: userId={}, 在线: {}", userId, onlineUserManager.getOnlineCount());
        }
    }

    @OnError
    public void onError(WebSocketSession session, Throwable error) {
        log.error("WebSocket 错误: sessionId={}, userId={}", session.getId(), session.getUserId(), error);
    }

    private void handleChatMessage(WebSocketSession session, JSONObject msgJson, String userId) {
        String chatType = msgJson.getStr("chatType", "PRIVATE");
        String msgType = msgJson.getStr("msgType", "TEXT");
        Long targetId = msgJson.getLong("targetId");
        String content = msgJson.getStr("content");
        Long fileId = msgJson.getLong("fileId");
        String fileName = msgJson.getStr("fileName");
        String contentType = msgJson.getStr("contentType");

        if ("IMAGE".equals(msgType) || "FILE".equals(msgType) || "AUDIO".equals(msgType)) {
            String dataBase64 = msgJson.getStr("data");
            if (dataBase64 != null && !dataBase64.isEmpty() && fileId == null) {
                byte[] fileData = Base64.getDecoder().decode(dataBase64);
                try {
                    ChatFile chatFile = chatFileService.storeFromBytes(
                            fileData,
                            fileName != null ? fileName : "file_" + System.currentTimeMillis(),
                            contentType != null ? contentType : "application/octet-stream",
                            Long.valueOf(userId));
                    fileId = chatFile.getId();
                } catch (Exception e) {
                    log.error("文件存储失败: userId={}", userId, e);
                    return;
                }
            }
        }

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSenderId(Long.valueOf(userId));
        chatMessage.setChatType(chatType);
        chatMessage.setMsgType(msgType);
        chatMessage.setContent(content);
        chatMessage.setFileId(fileId);
        chatMessage.setStatus(0);

        String nickname = session.getAttribute("nickname");

        if ("PRIVATE".equals(chatType)) {
            chatMessage.setReceiverId(targetId);
            chatMessageService.save(chatMessage);

            JSONObject pushMsg = buildPushMessage(chatMessage, nickname, fileId, fileName);
            onlineUserManager.sendToUser(String.valueOf(targetId), pushMsg.toString());
            session.sendMessage(pushMsg.toString());

            conversationService.updateConversation(Long.valueOf(userId), targetId, "PRIVATE", chatMessage, false);
            conversationService.updateConversation(targetId, Long.valueOf(userId), "PRIVATE", chatMessage, true);

        } else if ("GROUP".equals(chatType)) {
            chatMessage.setGroupId(targetId);
            chatMessageService.save(chatMessage);

            JSONObject pushMsg = buildPushMessage(chatMessage, nickname, fileId, fileName);
            List<Long> memberIds = groupService.getMemberUserIds(targetId);
            for (Long memberId : memberIds) {
                onlineUserManager.sendToUser(String.valueOf(memberId), pushMsg.toString());
                if (!memberId.equals(Long.valueOf(userId))) {
                    conversationService.updateConversation(memberId, targetId, "GROUP", chatMessage, true);
                }
            }
            conversationService.updateConversation(Long.valueOf(userId), targetId, "GROUP", chatMessage, false);
        }
    }

    private void handleReadReceipt(JSONObject msgJson, String userId) {
        Long senderId = msgJson.getLong("targetId");
        if (senderId == null) {
            return;
        }
        chatMessageService.markAsRead(Long.valueOf(userId), senderId);
        conversationService.clearUnread(Long.valueOf(userId), senderId, "PRIVATE");

        onlineUserManager.sendToUser(String.valueOf(senderId), new JSONObject()
                .set("action", "READ_RECEIPT")
                .set("readerId", userId)
                .set("timestamp", System.currentTimeMillis())
                .toString());
    }

    private void handleFriendRequest(JSONObject msgJson, String userId) {
        Long friendId = msgJson.getLong("targetId");
        String requestMsg = msgJson.getStr("content", "");

        ChatFriendship fs = friendshipService.sendRequest(Long.valueOf(userId), friendId, requestMsg);
        ChatUser user = chatUserService.getById(Long.valueOf(userId));

        onlineUserManager.sendToUser(String.valueOf(friendId), new JSONObject()
                .set("action", "FRIEND_REQUEST")
                .set("requestId", fs.getId())
                .set("userId", userId)
                .set("nickname", user != null ? user.getNickname() : "")
                .set("requestMsg", requestMsg)
                .set("timestamp", System.currentTimeMillis())
                .toString());
    }

    private void handleFriendAccept(JSONObject msgJson, String userId) {
        Long requestId = msgJson.getLong("requestId");
        boolean ok = friendshipService.acceptRequest(requestId, Long.valueOf(userId));
        if (ok) {
            ChatFriendship fs = friendshipService.getById(requestId);
            if (fs != null) {
                ChatUser user = chatUserService.getById(Long.valueOf(userId));
                onlineUserManager.sendToUser(String.valueOf(fs.getUserId()), new JSONObject()
                        .set("action", "FRIEND_ACCEPTED")
                        .set("friendId", userId)
                        .set("nickname", user != null ? user.getNickname() : "")
                        .set("timestamp", System.currentTimeMillis())
                        .toString());
            }
        }
    }

    private JSONObject buildPushMessage(ChatMessage msg, String nickname, Long fileId, String fileName) {
        return new JSONObject()
                .set("action", "CHAT_MSG")
                .set("messageId", msg.getId())
                .set("senderId", msg.getSenderId())
                .set("nickname", nickname)
                .set("chatType", msg.getChatType())
                .set("msgType", msg.getMsgType())
                .set("content", msg.getContent())
                .set("fileId", fileId)
                .set("fileName", fileName)
                .set("groupId", msg.getGroupId())
                .set("receiverId", msg.getReceiverId())
                .set("timestamp", System.currentTimeMillis());
    }

    private void pushOfflineMessages(WebSocketSession session, String userId) {
        List<ChatMessage> offlineMessages = chatMessageService.getOfflineMessages(Long.valueOf(userId));
        for (ChatMessage msg : offlineMessages) {
            ChatUser sender = chatUserService.getById(msg.getSenderId());
            String nickname = sender != null ? sender.getNickname() : "Unknown";
            session.sendMessage(buildPushMessage(msg, nickname, msg.getFileId(), msg.getContent()).toString());
        }
        if (!offlineMessages.isEmpty()) {
            log.info("推送离线消息: userId={}, count={}", userId, offlineMessages.size());
        }
    }

    private void pushOnlineStatus(String userId, boolean online) {
        ChatUser user = chatUserService.getById(Long.valueOf(userId));
        String nickname = user != null ? user.getNickname() : "";

        List<ChatFriendship> friends = friendshipService.getFriendList(Long.valueOf(userId));
        JSONObject statusMsg = new JSONObject()
                .set("action", "ONLINE_STATUS")
                .set("userId", userId)
                .set("nickname", nickname)
                .set("online", online)
                .set("onlineCount", onlineUserManager.getOnlineCount())
                .set("timestamp", System.currentTimeMillis());

        for (ChatFriendship fs : friends) {
            onlineUserManager.sendToUser(String.valueOf(fs.getFriendId()), statusMsg.toString());
        }
    }
}
