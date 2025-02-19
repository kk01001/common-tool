package io.github.kk01001.disruptor.factory;

import com.lmax.disruptor.EventFactory;
import io.github.kk01001.disruptor.event.DisruptorEvent;

/**
 * @author kk01001
 * @date 2025-02-19 10:15:47
 * @description Disruptor事件工厂类，用于创建事件对象
 */
public class DisruptorEventFactory<T> implements EventFactory<DisruptorEvent<T>> {

    @Override
    public DisruptorEvent<T> newInstance() {
        return new DisruptorEvent<>();
    }
}