package io.github.kk01001.sse.listener;

import cn.hutool.json.JSONUtil;
import io.github.kk01001.sse.manager.SseConnectionManager;
import io.github.kk01001.sse.model.SseMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;

/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description RocketMQ消息监听器
 */
@Slf4j
@RocketMQMessageListener(
        topic = "${sse.rocketMq.topic:sse-message}",
        consumerGroup = "${sse.rocketMq.consumerGroup:sse-consumer-group}")
public class RocketMqMessageListener implements RocketMQListener<SseMessage> {

    /**
     * SSE连接管理器
     */
    private final SseConnectionManager connectionManager;

    public RocketMqMessageListener(SseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public void onMessage(SseMessage sseMessage) {
        try {
            log.debug("收到RocketMQ消息: {}", JSONUtil.toJsonStr(sseMessage));

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
            log.error("处理RocketMQ消息异常", e);
        }
    }
}
