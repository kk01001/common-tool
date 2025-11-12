package io.github.kk01001.mqtt.metrics;

import lombok.Data;

import java.util.concurrent.atomic.AtomicLong;

/**
 * MQTT 指标统计
 *
 * @author kk01001
 */
@Data
public class MqttMetrics {

    /**
     * 发送消息总数
     */
    private final AtomicLong sentMessages = new AtomicLong(0);

    /**
     * 发送失败总数
     */
    private final AtomicLong sentFailures = new AtomicLong(0);

    /**
     * 接收消息总数
     */
    private final AtomicLong receivedMessages = new AtomicLong(0);

    /**
     * 接收处理失败总数
     */
    private final AtomicLong receivedFailures = new AtomicLong(0);

    /**
     * 连接次数
     */
    private final AtomicLong connections = new AtomicLong(0);

    /**
     * 断开连接次数
     */
    private final AtomicLong disconnections = new AtomicLong(0);

    /**
     * 重连次数
     */
    private final AtomicLong reconnections = new AtomicLong(0);

    /**
     * 记录发送消息
     */
    public void recordSent() {
        sentMessages.incrementAndGet();
    }

    /**
     * 记录发送失败
     */
    public void recordSentFailure() {
        sentFailures.incrementAndGet();
    }

    /**
     * 记录接收消息
     */
    public void recordReceived() {
        receivedMessages.incrementAndGet();
    }

    /**
     * 记录接收失败
     */
    public void recordReceivedFailure() {
        receivedFailures.incrementAndGet();
    }

    /**
     * 记录连接
     */
    public void recordConnection() {
        connections.incrementAndGet();
    }

    /**
     * 记录断开连接
     */
    public void recordDisconnection() {
        disconnections.incrementAndGet();
    }

    /**
     * 记录重连
     */
    public void recordReconnection() {
        reconnections.incrementAndGet();
    }

    /**
     * 重置所有指标
     */
    public void reset() {
        sentMessages.set(0);
        sentFailures.set(0);
        receivedMessages.set(0);
        receivedFailures.set(0);
        connections.set(0);
        disconnections.set(0);
        reconnections.set(0);
    }
}
