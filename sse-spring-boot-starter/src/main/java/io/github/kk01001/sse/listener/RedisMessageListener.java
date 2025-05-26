package io.github.kk01001.sse.listener;

import com.alibaba.fastjson.JSON;
import io.github.kk01001.sse.manager.SseConnectionManager;
import io.github.kk01001.sse.model.SseMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description Redis消息监听器
 */
@Slf4j
public class RedisMessageListener implements MessageListener {

    /**
     * SSE连接管理器
     */
    private final SseConnectionManager connectionManager;

    /**
     * Redis字符串序列化器
     */
    private final RedisSerializer<String> serializer = new StringRedisSerializer();

    public RedisMessageListener(SseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String messageJson = serializer.deserialize(message.getBody());
            SseMessage sseMessage = JSON.parseObject(messageJson, SseMessage.class);
            log.debug("收到Redis消息: {}", messageJson);

            // 处理消息
            if (sseMessage != null) {
                // 根据消息类型选择发送方式
                if (sseMessage.getClientId() != null) {
                    connectionManager.sendMessageToClient(sseMessage.getClientId(), sseMessage);
                    return;
                }
                if (sseMessage.getUserId() != null) {
                    connectionManager.sendMessageToUser(sseMessage.getUserId(), sseMessage);
                    return;
                }
                if (sseMessage.getTopic() != null) {
                    connectionManager.sendMessageToTopic(sseMessage.getTopic(), sseMessage);
                    return;
                }
                if (Boolean.TRUE.equals(sseMessage.getBroadcast())) {
                    connectionManager.broadcastMessage(sseMessage);
                }
            }
        } catch (Exception e) {
            log.error("处理Redis消息异常", e);
        }
    }
}
