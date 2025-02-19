package io.github.kk01001.disruptor.handler;

import com.lmax.disruptor.EventHandler;
import io.github.kk01001.disruptor.event.DisruptorEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * @author kk01001
 * @date 2025-02-19 10:15:47
 * @description Disruptor事件处理器，用于处理RingBuffer中的事件
 */
@Slf4j
public class DisruptorHandler<T> implements EventHandler<DisruptorEvent<T>> {
    
    @Override
    public void onEvent(DisruptorEvent<T> event, long sequence, boolean endOfBatch) {
        log.info("Received event: {}", event.getData());
        // 在这里处理事件
    }
} 