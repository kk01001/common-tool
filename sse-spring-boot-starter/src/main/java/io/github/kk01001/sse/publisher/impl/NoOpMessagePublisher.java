package io.github.kk01001.sse.publisher.impl;

import io.github.kk01001.sse.model.SseMessage;
import io.github.kk01001.sse.publisher.SseMessagePublisher;
import lombok.extern.slf4j.Slf4j;

/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description 空实现的消息发布者
 */
@Slf4j
public class NoOpMessagePublisher implements SseMessagePublisher {

    @Override
    public void publishMessage(SseMessage message) {
        // 不做任何操作
        log.debug("使用空实现的消息发布者，消息不会被转发到集群");
    }
}
