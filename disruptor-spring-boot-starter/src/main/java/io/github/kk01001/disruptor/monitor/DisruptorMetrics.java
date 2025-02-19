package io.github.kk01001.disruptor.monitor;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import io.github.kk01001.disruptor.event.DisruptorEvent;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class DisruptorMetrics {

    private final Map<String, Disruptor<DisruptorEvent<Object>>> disruptorMap;

    public DisruptorMetrics(Map<String, Disruptor<DisruptorEvent<Object>>> disruptorMap, MeterRegistry meterRegistry) {
        this.disruptorMap = disruptorMap;
        disruptorMap.forEach((name, disruptor) -> {
            Gauge.builder("disruptor.buffer.size", disruptor.getRingBuffer(), RingBuffer::getBufferSize)
                    .tag("queue", name)
                    .register(meterRegistry);

            Gauge.builder("disruptor.remaining.capacity", disruptor.getRingBuffer(), RingBuffer::remainingCapacity)
                    .tag("queue", name)
                    .register(meterRegistry);
        });
    }
} 