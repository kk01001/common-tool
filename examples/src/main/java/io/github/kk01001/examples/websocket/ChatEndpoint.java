package io.github.kk01001.examples.websocket;

import io.github.kk01001.netty.annotation.*;
import io.github.kk01001.netty.session.WebSocketSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@WebSocketEndpoint(path = "/chat")
public class ChatEndpoint {

    @OnOpen
    public void onOpen(WebSocketSession session) {
        log.info("新用户加入: sessionId={}", session.getId());
        // 广播新用户加入消息
        session.broadcast("系统消息：新用户 " + session.getId() + " 加入聊天室");
    }

    @OnMessage
    public void onMessage(WebSocketSession session, String message) {
        log.info("收到消息: sessionId={}, message={}", session.getId(), message);
        // 广播消息给其他用户
        session.broadcast("用户 " + session.getId() + " 说：" + message);
    }

    @OnClose
    public void onClose(WebSocketSession session) {
        log.info("用户离开: sessionId={}", session.getId());
        // 广播用户离开消息
        session.broadcast("系统消息：用户 " + session.getId() + " 离开聊天室");
    }

    @OnError
    public void onError(WebSocketSession session, Throwable error) {
        log.error("发生错误: sessionId={}", session.getId(), error);
    }
} 