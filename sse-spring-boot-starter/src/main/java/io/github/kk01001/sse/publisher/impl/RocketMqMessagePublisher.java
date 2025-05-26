package io.github.kk01001.sse.publisher.impl;

import com.alibaba.fastjson.JSON;
import io.github.kk01001.sse.config.SseProperties;
import io.github.kk01001.sse.model.SseMessage;
import io.github.kk01001.sse.publisher.SseMessagePublisher;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.support.MessageBuilder;

/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description 基于RocketMQ的消息发布者实现
 */
@Slf4j
public class RocketMqMessagePublisher implements SseMessagePublisher {

    /**
     * RocketMQ模板
     */
    private final RocketMQTemplate rocketMQTemplate;

    /**
     * SSE配置属性
     */
    private final SseProperties properties;

    public RocketMqMessagePublisher(RocketMQTemplate rocketMQTemplate, SseProperties properties) {
        this.rocketMQTemplate = rocketMQTemplate;
        this.properties = properties;
    }

    @Override
    public void publishMessage(SseMessage message) {
        if (message == null) {
            return;
        }

        try {
            String messageJson = JSON.toJSONString(message);
            rocketMQTemplate.syncSend(properties.getRocketMq().getTopic(),
                    MessageBuilder.withPayload(messageJson).build());
            log.debug("RocketMQ发布消息成功: {}", messageJson);
        } catch (Exception e) {
            log.error("RocketMQ发布消息失败", e);
        }
    }
}
