package io.github.kk01001.mqtt.example;

import io.github.kk01001.mqtt.annotation.MqttMessageListener;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * MQTT 消费者示例
 * 演示如何使用 @MqttMessageListener 注解接收消息
 *
 * @author kk01001
 */
@Slf4j
@Component
public class MqttConsumerExample {

    /**
     * 示例1：接收单个主题的消息（只接收消息内容）
     */
    @MqttMessageListener(topics = "testtopic/1", qos = 1)
    public void handleMessage1(String payload) {
        log.info("【监听器1】收到消息 - 内容: {}", payload);
    }

    /**
     * 示例2：接收多个主题的消息（接收主题和消息内容）
     */
    @MqttMessageListener(topics = {"testtopic/2", "testtopic/3"}, qos = 1)
    public void handleMessage2(String topic, String payload) {
        log.info("【监听器2】收到消息 - 主题: {}, 内容: {}", topic, payload);
    }

    /**
     * 示例3：使用通配符订阅（接收 MqttMessage 对象）
     */
    @MqttMessageListener(topics = "testtopic/#", qos = 2)
    public void handleMessage3(String topic, MqttMessage message) {
        String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
        log.info("【监听器3-通配符】收到消息 - 主题: {}, QoS: {}, Retained: {}, 内容: {}", 
                topic, message.getQos(), message.isRetained(), payload);
    }

    /**
     * 示例4：接收字节数组格式的消息
     */
    @MqttMessageListener(topics = "testtopic/binary", qos = 1)
    public void handleBinaryMessage(String topic, byte[] payload) {
        log.info("【监听器4】收到二进制消息 - 主题: {}, 长度: {} bytes", topic, payload.length);
    }

    /**
     * 示例5：使用单层通配符
     */
    @MqttMessageListener(topics = "testtopic/+/status", qos = 1)
    public void handleStatusMessage(String topic, String payload) {
        log.info("【监听器5-单层通配符】收到状态消息 - 主题: {}, 内容: {}", topic, payload);
    }

    /**
     * 示例6：接收 JSON 消息
     */
    @MqttMessageListener(topics = "testtopic/json", qos = 1)
    public void handleJsonMessage(String topic, String payload) {
        log.info("【监听器6】收到 JSON 消息 - 主题: {}, 内容: {}", topic, payload);
        // 可以在这里解析 JSON
    }

    /**
     * 示例7：接收异步消息
     */
    @MqttMessageListener(topics = "testtopic/async", qos = 1)
    public void handleAsyncMessage(String topic, String payload) {
        log.info("【监听器7】收到异步消息 - 主题: {}, 内容: {}", topic, payload);
    }

    /**
     * 示例8：接收批量消息
     */
    @MqttMessageListener(topics = "testtopic/batch", qos = 1)
    public void handleBatchMessage(String topic, String payload) {
        log.info("【监听器8】收到批量消息 - 主题: {}, 内容: {}", topic, payload);
    }
}
