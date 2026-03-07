package io.github.kk01001.chat.example.ws;

import io.github.kk01001.netty.session.WebSocketSession;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author kk01001
 * @date 2026-03-07 18:00:00
 * @description 在线用户管理器
 */
@Component
public class OnlineUserManager {

    /**
     * userId -> WebSocketSession
     */
    private final Map<String, WebSocketSession> onlineSessions = new ConcurrentHashMap<>();

    public void addUser(String userId, WebSocketSession session) {
        onlineSessions.put(userId, session);
    }

    public void removeUser(String userId) {
        onlineSessions.remove(userId);
    }

    public WebSocketSession getSession(String userId) {
        return onlineSessions.get(userId);
    }

    public boolean isOnline(String userId) {
        WebSocketSession session = onlineSessions.get(userId);
        return session != null && session.isActive();
    }

    public Set<String> getOnlineUserIds() {
        return onlineSessions.keySet();
    }

    public int getOnlineCount() {
        return onlineSessions.size();
    }

    /**
     * 向指定用户发送消息
     */
    public boolean sendToUser(String userId, String message) {
        WebSocketSession session = onlineSessions.get(userId);
        if (session != null && session.isActive()) {
            session.sendMessage(message);
            return true;
        }
        return false;
    }
}
