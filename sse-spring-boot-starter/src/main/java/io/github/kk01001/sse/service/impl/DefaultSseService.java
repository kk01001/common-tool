package io.github.kk01001.sse.service.impl;

import cn.hutool.core.util.IdUtil;
import io.github.kk01001.sse.manager.SseConnectionManager;
import io.github.kk01001.sse.model.SseMessage;
import io.github.kk01001.sse.service.SseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description 默认SSE服务实现
 */
@RequiredArgsConstructor
public class DefaultSseService implements SseService {

    /**
     * SSE连接管理器
     */
    private final SseConnectionManager connectionManager;

    @Override
    public SseEmitter createConnection(String clientId, String userId, String topic) {
        return connectionManager.createConnection(clientId, userId, topic);
    }

    @Override
    public void closeConnection(String clientId) {
        connectionManager.closeConnection(clientId);
    }

    @Override
    public void sendMessage(SseMessage message) {
        connectionManager.sendMessage(message);
    }

    @Override
    public void sendMessageToClient(String clientId, SseMessage message) {
        message.setClientId(clientId);
        message.setUserId(null);
        message.setTopic(null);
        message.setBroadcast(false);
        connectionManager.sendMessage(message);
    }

    @Override
    public void sendMessageToClient(String clientId, String data, String event) {
        SseMessage message = SseMessage.builder()
                .id(IdUtil.fastSimpleUUID())
                .clientId(clientId)
                .data(data)
                .event(event)
                .build();
        connectionManager.sendMessage(message);
    }

    @Override
    public void sendMessageToUser(String userId, SseMessage message) {
        message.setUserId(userId);
        message.setClientId(null);
        message.setTopic(null);
        message.setBroadcast(false);
        connectionManager.sendMessage(message);
    }

    @Override
    public void sendMessageToUser(String userId, String data, String event) {
        SseMessage message = SseMessage.builder()
                .id(IdUtil.fastSimpleUUID())
                .userId(userId)
                .data(data)
                .event(event)
                .build();
        connectionManager.sendMessage(message);
    }

    @Override
    public void sendMessageToTopic(String topic, SseMessage message) {
        message.setTopic(topic);
        message.setClientId(null);
        message.setUserId(null);
        message.setBroadcast(false);
        connectionManager.sendMessage(message);
    }

    @Override
    public void sendMessageToTopic(String topic, String data, String event) {
        SseMessage message = SseMessage.builder()
                .id(IdUtil.fastSimpleUUID())
                .topic(topic)
                .data(data)
                .event(event)
                .build();
        connectionManager.sendMessage(message);
    }

    @Override
    public void broadcastMessage(SseMessage message) {
        message.setBroadcast(true);
        message.setTopic(null);
        message.setClientId(null);
        message.setUserId(null);
        connectionManager.sendMessage(message);
    }

    @Override
    public void broadcastMessage(String data, String event) {
        SseMessage message = SseMessage.builder()
                .id(IdUtil.fastSimpleUUID())
                .data(data)
                .event(event)
                .broadcast(true)
                .build();
        connectionManager.sendMessage(message);
    }

    @Override
    public int getConnectionCount() {
        return connectionManager.getConnectionCount();
    }
}
