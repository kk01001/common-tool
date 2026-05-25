package io.github.archer099.disruptor.event;

import lombok.Data;

/**
 * @author archer099
 * @date 2025-02-19 10:15:47
 * @description Disruptor事件包装类，用于在RingBuffer中传递消息
 */
@Data
public class DisruptorEvent<T> {
    private T data;
} 