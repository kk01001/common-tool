package io.github.kk01001.disruptor.monitor;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import io.github.kk01001.disruptor.event.DisruptorEvent;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;

/**
 * @author kk01001
 * @date 2025-02-19 10:15:47
 * @description Disruptor 监控类，用于监控 Disruptor 队列的缓冲区大小和剩余容量
 */
@Slf4j
public class DisruptorMetrics {

    private final MeterRegistry meterRegistry;

    public DisruptorMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * 手动注册 Disruptor 队列以进行监控
     *
     * @param queueName 队列名称
     * @param disruptor Disruptor 实例
     */
    public void registerManualDisruptor(String queueName, Disruptor<DisruptorEvent<Object>> disruptor) {
        registerMetrics(queueName, disruptor);
    }

    /**
     * 注册监控指标
     *
     * @param queueName 队列名称
     * @param disruptor Disruptor 实例
     */
    private void registerMetrics(String queueName, Disruptor<DisruptorEvent<Object>> disruptor) {
        if (disruptor == null) {
            log.warn("Disruptor instance is null for queue: {}", queueName);
            return;
        }

        RingBuffer<?> ringBuffer = disruptor.getRingBuffer();
        if (ringBuffer == null) {
            log.warn("RingBuffer is null for queue: {}", queueName);
            return;
        }

        // 注册缓冲区大小指标
        Gauge.builder("disruptor.buffer.size", ringBuffer, RingBuffer::getBufferSize)
                .tag("queue", queueName)
                .description("The total size of the Disruptor ring buffer")
                .register(meterRegistry);

        // 注册剩余容量指标
        Gauge.builder("disruptor.remaining.capacity", ringBuffer, RingBuffer::remainingCapacity)
                .tag("queue", queueName)
                .description("The remaining capacity of the Disruptor ring buffer")
                .register(meterRegistry);

        log.info("Registered metrics for Disruptor queue: {}", queueName);
    }
} 