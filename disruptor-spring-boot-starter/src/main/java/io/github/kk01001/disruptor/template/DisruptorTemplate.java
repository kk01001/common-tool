package io.github.kk01001.disruptor.template;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import io.github.kk01001.disruptor.event.DisruptorEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author kk01001
 * @date 2025-02-19 10:15:47
 * @description Disruptor模板类，提供消息发送的统一接口
 */
@Slf4j
public class DisruptorTemplate {

    private final Map<String, Disruptor<DisruptorEvent<Object>>> disruptorMap = new ConcurrentHashMap<>();

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
} 