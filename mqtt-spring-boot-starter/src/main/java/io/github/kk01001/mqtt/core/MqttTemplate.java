package io.github.archer099.mqtt.core;

import io.github.archer099.mqtt.config.MqttProperties;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

/**
 * MQTT 消息发送模板
 * 提供便捷的消息发送方法
 *
 * @author archer099
 */
@Slf4j
public class MqttTemplate {

    private final MqttClientManager clientManager;
    private final MqttProperties properties;

    public MqttTemplate(MqttClientManager clientManager, MqttProperties properties) {
        this.clientManager = clientManager;
        this.properties = properties;
    }

    /**
     * 发送消息（使用默认 QoS 和 retained）
     *
     * @param topic   主题
     * @param payload 消息内容
     */
    public void send(String topic, String payload) throws MqttException {
        send(topic, payload, properties.getProducer().getDefaultQos(), properties.getProducer().getDefaultRetained());
    }

    /**
     * 发送消息（使用默认 QoS 和 retained）
     *
     * @param topic   主题
     * @param payload 消息内容（字节数组）
     */
    public void send(String topic, byte[] payload) throws MqttException {
        send(topic, payload, properties.getProducer().getDefaultQos(), properties.getProducer().getDefaultRetained());
    }

    /**
     * 发送消息（指定 QoS）
     *
     * @param topic   主题
     * @param payload 消息内容
     * @param qos     QoS 级别
     */
    public void send(String topic, String payload, int qos) throws MqttException {
        send(topic, payload, qos, properties.getProducer().getDefaultRetained());
    }

    /**
     * 发送消息（指定 QoS 和 retained）
     *
     * @param topic    主题
     * @param payload  消息内容
     * @param qos      QoS 级别
     * @param retained 是否保留消息
     */
    public void send(String topic, String payload, int qos, boolean retained) throws MqttException {
        send(topic, payload.getBytes(StandardCharsets.UTF_8), qos, retained);
    }

    /**
     * 发送消息（指定 QoS 和 retained）
     *
     * @param topic    主题
     * @param payload  消息内容（字节数组）
     * @param qos      QoS 级别
     * @param retained 是否保留消息
     */
    public void send(String topic, byte[] payload, int qos, boolean retained) throws MqttException {
        MqttMessage message = new MqttMessage(payload);
        message.setQos(qos);
        message.setRetained(retained);
        clientManager.publish(topic, message);
    }

    /**
     * 异步发送消息（使用默认 QoS 和 retained）
     *
     * @param topic   主题
     * @param payload 消息内容
     * @return CompletableFuture
     */
    public CompletableFuture<Void> sendAsync(String topic, String payload) {
        return sendAsync(topic, payload, properties.getProducer().getDefaultQos(), properties.getProducer().getDefaultRetained());
    }

    /**
     * 异步发送消息（使用默认 QoS 和 retained）
     *
     * @param topic   主题
     * @param payload 消息内容（字节数组）
     * @return CompletableFuture
     */
    public CompletableFuture<Void> sendAsync(String topic, byte[] payload) {
        return sendAsync(topic, payload, properties.getProducer().getDefaultQos(), properties.getProducer().getDefaultRetained());
    }

    /**
     * 异步发送消息（指定 QoS）
     *
     * @param topic   主题
     * @param payload 消息内容
     * @param qos     QoS 级别
     * @return CompletableFuture
     */
    public CompletableFuture<Void> sendAsync(String topic, String payload, int qos) {
        return sendAsync(topic, payload, qos, properties.getProducer().getDefaultRetained());
    }

    /**
     * 异步发送消息（指定 QoS 和 retained）
     *
     * @param topic    主题
     * @param payload  消息内容
     * @param qos      QoS 级别
     * @param retained 是否保留消息
     * @return CompletableFuture
     */
    public CompletableFuture<Void> sendAsync(String topic, String payload, int qos, boolean retained) {
        return sendAsync(topic, payload.getBytes(StandardCharsets.UTF_8), qos, retained);
    }

    /**
     * 异步发送消息（指定 QoS 和 retained）
     *
     * @param topic    主题
     * @param payload  消息内容（字节数组）
     * @param qos      QoS 级别
     * @param retained 是否保留消息
     * @return CompletableFuture
     */
    public CompletableFuture<Void> sendAsync(String topic, byte[] payload, int qos, boolean retained) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        try {
            MqttMessage message = new MqttMessage(payload);
            message.setQos(qos);
            message.setRetained(retained);

            clientManager.publishAsync(topic, message, new IMqttActionListener() {
                @Override
                public void onSuccess(org.eclipse.paho.client.mqttv3.IMqttToken asyncActionToken) {
                    log.debug("异步发送消息成功: topic={}", topic);
                    future.complete(null);
                }

                @Override
                public void onFailure(org.eclipse.paho.client.mqttv3.IMqttToken asyncActionToken, Throwable exception) {
                    log.error("异步发送消息失败: topic={}", topic, exception);
                    future.completeExceptionally(exception);
                }
            });
        } catch (Exception e) {
            log.error("异步发送消息异常: topic={}", topic, e);
            future.completeExceptionally(e);
        }

        return future;
    }

    public void sendDelayed(String topic, String payload, int delaySeconds) throws MqttException {
        if (delaySeconds <= 0 || delaySeconds > 4_294_967) {
            throw new IllegalArgumentException("invalid delaySeconds");
        }
        String delayedTopic = "$delayed/" + delaySeconds + "/" + topic;
        send(delayedTopic, payload, properties.getProducer().getDefaultQos(), properties.getProducer().getDefaultRetained());
    }

    public void sendDelayed(String topic, byte[] payload, int delaySeconds) throws MqttException {
        if (delaySeconds <= 0 || delaySeconds > 4_294_967) {
            throw new IllegalArgumentException("invalid delaySeconds");
        }
        String delayedTopic = "$delayed/" + delaySeconds + "/" + topic;
        send(delayedTopic, payload, properties.getProducer().getDefaultQos(), properties.getProducer().getDefaultRetained());
    }

    public CompletableFuture<Void> sendDelayedAsync(String topic, String payload, int delaySeconds) {
        if (delaySeconds <= 0 || delaySeconds > 4_294_967) {
            CompletableFuture<Void> f = new CompletableFuture<>();
            f.completeExceptionally(new IllegalArgumentException("invalid delaySeconds"));
            return f;
        }
        String delayedTopic = "$delayed/" + delaySeconds + "/" + topic;
        return sendAsync(delayedTopic, payload, properties.getProducer().getDefaultQos(), properties.getProducer().getDefaultRetained());
    }

    public CompletableFuture<Void> sendDelayedAsync(String topic, byte[] payload, int delaySeconds) {
        if (delaySeconds <= 0 || delaySeconds > 4_294_967) {
            CompletableFuture<Void> f = new CompletableFuture<>();
            f.completeExceptionally(new IllegalArgumentException("invalid delaySeconds"));
            return f;
        }
        String delayedTopic = "$delayed/" + delaySeconds + "/" + topic;
        return sendAsync(delayedTopic, payload, properties.getProducer().getDefaultQos(), properties.getProducer().getDefaultRetained());
    }

    /**
     * 发送 MqttMessage 对象
     *
     * @param topic   主题
     * @param message MQTT 消息对象
     */
    public void send(String topic, MqttMessage message) throws MqttException {
        clientManager.publish(topic, message);
    }

    /**
     * 异步发送 MqttMessage 对象
     *
     * @param topic   主题
     * @param message MQTT 消息对象
     * @return CompletableFuture
     */
    public CompletableFuture<Void> sendAsync(String topic, MqttMessage message) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        try {
            clientManager.publishAsync(topic, message, new IMqttActionListener() {
                @Override
                public void onSuccess(org.eclipse.paho.client.mqttv3.IMqttToken asyncActionToken) {
                    future.complete(null);
                }

                @Override
                public void onFailure(org.eclipse.paho.client.mqttv3.IMqttToken asyncActionToken, Throwable exception) {
                    future.completeExceptionally(exception);
                }
            });
        } catch (Exception e) {
            future.completeExceptionally(e);
        }

        return future;
    }

    /**
     * 订阅主题
     *
     * @param topic 主题
     * @param qos   QoS 级别
     */
    public void subscribe(String topic, int qos) throws MqttException {
        clientManager.subscribe(topic, qos, (t, msg) -> {
            log.info("收到消息 - 主题: {}, QoS: {}, 内容: {}", t, msg.getQos(), new String(msg.getPayload()));
        });
    }

    /**
     * 取消订阅
     *
     * @param topic 主题
     */
    public void unsubscribe(String topic) throws MqttException {
        clientManager.unsubscribe(topic);
    }

    /**
     * 检查是否已连接
     */
    public boolean isConnected() {
        return clientManager.isConnected();
    }
}
