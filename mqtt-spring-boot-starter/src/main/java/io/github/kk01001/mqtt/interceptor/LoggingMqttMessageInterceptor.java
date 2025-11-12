package io.github.kk01001.mqtt.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.charset.StandardCharsets;

/**
 * MQTT 消息日志拦截器
 * 记录所有消息的发送和接收日志
 *
 * @author kk01001
 */
@Slf4j
public class LoggingMqttMessageInterceptor implements MqttMessageInterceptor {

    private final boolean logPayload;

    public LoggingMqttMessageInterceptor() {
        this(true);
    }

    public LoggingMqttMessageInterceptor(boolean logPayload) {
        this.logPayload = logPayload;
    }

    @Override
    public boolean beforeSend(String topic, MqttMessage message) {
        if (log.isDebugEnabled()) {
            if (logPayload) {
                log.debug("准备发送消息 - 主题: {}, QoS: {}, Retained: {}, Payload: {}",
                        topic, message.getQos(), message.isRetained(),
                        new String(message.getPayload(), StandardCharsets.UTF_8));
            } else {
                log.debug("准备发送消息 - 主题: {}, QoS: {}, Retained: {}, Size: {} bytes",
                        topic, message.getQos(), message.isRetained(), message.getPayload().length);
            }
        }
        return true;
    }

    @Override
    public void afterSend(String topic, MqttMessage message, boolean success) {
        if (success) {
            log.debug("消息发送成功 - 主题: {}", topic);
        } else {
            log.warn("消息发送失败 - 主题: {}", topic);
        }
    }

    @Override
    public boolean beforeReceive(String topic, MqttMessage message) {
        if (log.isDebugEnabled()) {
            if (logPayload) {
                log.debug("收到消息 - 主题: {}, QoS: {}, Payload: {}",
                        topic, message.getQos(),
                        new String(message.getPayload(), StandardCharsets.UTF_8));
            } else {
                log.debug("收到消息 - 主题: {}, QoS: {}, Size: {} bytes",
                        topic, message.getQos(), message.getPayload().length);
            }
        }
        return true;
    }

    @Override
    public int getOrder() {
        return Integer.MAX_VALUE; // 最低优先级，最后执行
    }
}
