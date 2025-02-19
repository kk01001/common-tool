package io.github.kk01001.disruptor.template;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import io.github.kk01001.disruptor.event.DisruptorEvent;
import io.github.kk01001.disruptor.factory.DisruptorEventFactory;
import io.github.kk01001.disruptor.handler.MessageHandler;
import io.github.kk01001.disruptor.handler.MessageHandlerAdapter;
import io.github.kk01001.disruptor.monitor.DisruptorMetrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;

/**
 * @author kk01001
 * @date 2025-02-19 10:15:47
 * @description Disruptor模板类，提供消息发送的统一接口
 */
@Slf4j
public class DisruptorTemplate {

    private final Map<String, Disruptor<DisruptorEvent<Object>>> disruptorMap = new ConcurrentHashMap<>();

    private final DisruptorMetrics disruptorMetrics;

    public DisruptorTemplate(DisruptorMetrics disruptorMetrics) {
        this.disruptorMetrics = disruptorMetrics;
    }

    /**
     * 发送消息到指定队列
     *
     * @param queueName 队列名称
     * @param data      消息数据
     */
    public <T> void send(String queueName, T data) {
        Assert.hasText(queueName, "Queue name must not be empty");
        Disruptor<DisruptorEvent<Object>> disruptor = disruptorMap.get(queueName);
        Assert.notNull(disruptor, "Queue " + queueName + " not found");

        // 获取 RingBuffer，避免多次调用
        final RingBuffer<DisruptorEvent<Object>> ringBuffer = disruptor.getRingBuffer();
        // 使用 Disruptor 提供的发布事件接口，更安全的发布方式
        ringBuffer.publishEvent((event, sequence, eventData) -> {
            event.setData(eventData);
            log.debug("Sending message to queue {}: {}", queueName, eventData);
        }, data);
    }

    /**
     * 注册Disruptor实例
     */
    public void registerDisruptor(String queueName, Disruptor<DisruptorEvent<Object>> disruptor) {
        disruptorMap.put(queueName, disruptor);
    }

    /**
     * 关闭指定队列的Disruptor实例
     *
     * @param queueName 队列名称
     */
    public void shutdown(String queueName) {
        Disruptor<DisruptorEvent<Object>> disruptor = disruptorMap.remove(queueName);
        if (disruptor != null) {
            disruptor.shutdown();
        }
    }

    /**
     * 关闭所有Disruptor实例
     */
    public void shutdownAll() {
        disruptorMap.forEach((name, disruptor) -> disruptor.shutdown());
        disruptorMap.clear();
    }

    /**
     * 注册 Disruptor 监控指标
     *
     * @param queueName 队列名称
     * @param disruptor Disruptor 实例
     */
    public void registerMetrics(String queueName, Disruptor<DisruptorEvent<Object>> disruptor) {
        if (disruptorMetrics != null) {
            disruptorMetrics.registerManualDisruptor(queueName, disruptor);
        }
    }

    /**
     * 创建并注册一个新的Disruptor队列
     *
     * @param queueName     队列名称
     * @param bufferSize    缓冲区大小
     * @param producerType  生产者类型
     * @param waitStrategy  等待策略
     * @param threadFactory 线程工厂
     * @param handler       消息处理器
     * @param <T>           消息类型
     * @return 创建的Disruptor实例
     */
    @SuppressWarnings({"unchecked", "rawtypes", "unused"})
    public <T> Disruptor<DisruptorEvent<T>> createQueue(
            String queueName,
            int bufferSize,
            ProducerType producerType,
            WaitStrategy waitStrategy,
            ThreadFactory threadFactory,
            MessageHandler<T> handler) {
        Assert.hasText(queueName, "Queue name must not be empty");
        Assert.isTrue(bufferSize > 0, "Buffer size must be positive");
        Assert.notNull(producerType, "Producer type must not be null");
        Assert.notNull(waitStrategy, "Wait strategy must not be null");
        Assert.notNull(threadFactory, "Thread factory must not be null");
        Assert.notNull(handler, "Handler must not be null");

        // 将MessageHandler适配为EventHandler
        EventHandler<DisruptorEvent<T>> eventHandler = new MessageHandlerAdapter<>(handler);

        DisruptorEventFactory<T> factory = new DisruptorEventFactory<>();
        Disruptor<DisruptorEvent<T>> disruptor = new Disruptor<>(
                factory,
                bufferSize,
                threadFactory,
                producerType,
                waitStrategy
        );

        disruptor.handleEventsWith(eventHandler);
        disruptor.start();
        registerDisruptor(queueName, (Disruptor) disruptor);
        registerMetrics(queueName, (Disruptor) disruptor);

        log.info("Created new Disruptor queue: {}, bufferSize: {}, producerType: {}, waitStrategy: {}",
                queueName, bufferSize, producerType, waitStrategy.getClass().getSimpleName());
        return disruptor;
    }
}