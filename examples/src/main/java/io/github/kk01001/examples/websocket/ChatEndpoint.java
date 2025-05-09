package io.github.kk01001.examples.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.kk01001.netty.annotation.OnBinaryMessage;
import io.github.kk01001.netty.annotation.OnClose;
import io.github.kk01001.netty.annotation.OnError;
import io.github.kk01001.netty.annotation.OnMessage;
import io.github.kk01001.netty.annotation.OnOpen;
import io.github.kk01001.netty.annotation.WebSocketEndpoint;
import io.github.kk01001.netty.session.WebSocketSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@WebSocketEndpoint
public class ChatEndpoint {

    private final ObjectMapper objectMapper;

    @OnOpen
    public void onOpen(WebSocketSession session) {
        log.info("新用户加入: sessionId={}", session.getId());
        // 发送加入消息
        ChatMessage joinMessage = new ChatMessage();
        joinMessage.setType(ChatMessage.MessageType.JOIN);
        joinMessage.setFromSessionId(session.getId());
        joinMessage.setContent("加入聊天室");
        broadcastMessage(session, joinMessage);
    }

    @OnBinaryMessage
    public void onBinaryMessage(WebSocketSession session, byte[] message) {
        log.info("收到二进制消息: sessionId={}, message={}", session.getId(), new String(message));
    }

    @OnMessage
    public void onMessage(WebSocketSession session, String message) {
        log.info("收到消息: sessionId={}, message={}", session.getId(), message);

        try {
            if ("ping".equals(message)) {
                session.sendMessage("pong");
                return;
            }
            if (message.startsWith("@")) {
                // 处理私聊消息
                int idx = message.indexOf(":");
                if (idx > 1) {
                    String targetId = message.substring(1, idx);
                    String content = message.substring(idx + 1);

                    ChatMessage privateMessage = new ChatMessage();
                    privateMessage.setType(ChatMessage.MessageType.PRIVATE);
                    privateMessage.setFromSessionId(session.getId());
                    privateMessage.setToSessionId(targetId);
                    privateMessage.setContent(content);

                    // 发送给目标用户
                    String json = objectMapper.writeValueAsString(privateMessage);
                    session.sendToSession(targetId, json);
                }
            } else {
                // 广播消息
                ChatMessage broadcastMessage = new ChatMessage();
                broadcastMessage.setType(ChatMessage.MessageType.BROADCAST);
                broadcastMessage.setFromSessionId(session.getId());
                broadcastMessage.setContent(message);
                broadcastMessage(session, broadcastMessage);
            }
        } catch (Exception e) {
            log.error("处理消息失败", e);
        }
    }

    @OnClose
    public void onClose(WebSocketSession session) {
        if (!Boolean.TRUE.equals(session.getAttribute("authSuccess"))) {
            return;
        }
        log.info("用户离开: sessionId={}", session.getId());
        // 发送离开消息
        ChatMessage leaveMessage = new ChatMessage();
        leaveMessage.setType(ChatMessage.MessageType.LEAVE);
        leaveMessage.setFromSessionId(session.getId());
        leaveMessage.setContent("离开聊天室");
        broadcastMessage(session, leaveMessage);
    }

    @OnError
    public void onError(WebSocketSession session, Throwable error) {
        log.error("发生错误: sessionId={}", session.getId(), error);
    }

    private void broadcastMessage(WebSocketSession session, ChatMessage message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            session.broadcast(json);
        } catch (Exception e) {
            log.error("广播消息失败", e);
        }
    }
} 