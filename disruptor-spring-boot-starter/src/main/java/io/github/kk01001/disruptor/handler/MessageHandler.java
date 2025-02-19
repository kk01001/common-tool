package io.github.kk01001.disruptor.handler;

import io.github.kk01001.disruptor.event.DisruptorEvent;

/**
 * @author kk01001
 * @date 2025-02-19 10:15:47
 * @description 消息处理器接口
 */
public interface MessageHandler<T> {
    void handle(DisruptorEvent<T> event);
} 