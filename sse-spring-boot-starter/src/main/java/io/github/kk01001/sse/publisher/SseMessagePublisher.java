package io.github.kk01001.sse.publisher;

import io.github.kk01001.sse.model.SseMessage;

/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description SSE消息发布者接口
 */
public interface SseMessagePublisher {

    /**
     * 发布消息
     *
     * @param message 消息
     */
    void publishMessage(SseMessage message);
}
