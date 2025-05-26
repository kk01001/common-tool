package io.github.kk01001.sse.manager;

import io.github.kk01001.sse.model.SseMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description SSE连接管理器接口
 */
public interface SseConnectionManager {

    /**
     * 创建SSE连接
     *
     * @param clientId 客户端ID
     * @param userId 用户ID
     * @param topic 主题
     * @return SseEmitter
     */
    SseEmitter createConnection(String clientId, String userId, String topic);

    /**
     * 关闭SSE连接
     *
     * @param clientId 客户端ID
     */
    void closeConnection(String clientId);

    /**
     * 发送消息
     *
     * @param message 消息
     */
    void sendMessage(SseMessage message);

    /**
     * 向指定客户端发送消息
     *
     * @param clientId 客户端ID
     * @param message 消息
     */
    void sendMessageToClient(String clientId, SseMessage message);

    /**
     * 向指定用户发送消息
     *
     * @param userId 用户ID
     * @param message 消息
     */
    void sendMessageToUser(String userId, SseMessage message);

    /**
     * 向指定主题发送消息
     *
     * @param topic 主题
     * @param message 消息
     */
    void sendMessageToTopic(String topic, SseMessage message);

    /**
     * 广播消息
     *
     * @param message 消息
     */
    void broadcastMessage(SseMessage message);

    /**
     * 获取连接数量
     *
     * @return 连接数量
     */
    int getConnectionCount();
}
