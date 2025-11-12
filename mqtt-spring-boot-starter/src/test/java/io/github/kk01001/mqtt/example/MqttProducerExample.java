package io.github.kk01001.mqtt.example;

import io.github.kk01001.mqtt.core.MqttTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * MQTT 生产者示例
 * 演示如何使用 MqttTemplate 发送消息
 *
 * @author kk01001
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MqttProducerExample {

    private final MqttTemplate mqttTemplate;

    /**
     * 示例1：发送简单消息（使用默认 QoS 和 retained）
     */
    public void sendSimpleMessage() throws MqttException {
        mqttTemplate.send("testtopic/1", "Hello MQTT!");
        log.info("发送简单消息成功");
    }

    /**
     * 示例2：发送消息并指定 QoS
     */
    public void sendMessageWithQos() throws MqttException {
        mqttTemplate.send("testtopic/2", "Hello with QoS 2", 2);
        log.info("发送 QoS 2 消息成功");
    }

    /**
     * 示例3：发送保留消息
     */
    public void sendRetainedMessage() throws MqttException {
        mqttTemplate.send("testtopic/3", "Retained message", 1, true);
        log.info("发送保留消息成功");
    }

    /**
     * 示例4：发送字节数组消息
     */
    public void sendBinaryMessage() throws MqttException {
        byte[] data = new byte[]{0x01, 0x02, 0x03, 0x04};
        mqttTemplate.send("testtopic/binary", data);
        log.info("发送二进制消息成功");
    }

    /**
     * 示例5：异步发送消息
     */
    public void sendAsyncMessage() {
        CompletableFuture<Void> future = mqttTemplate.sendAsync("testtopic/async", "Async message");
        
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("异步发送消息失败", ex);
            } else {
                log.info("异步发送消息成功");
            }
        });
    }

    /**
     * 示例6：批量发送消息
     */
    public void sendBatchMessages() throws MqttException {
        for (int i = 0; i < 10; i++) {
            mqttTemplate.send("testtopic/batch", "Message " + i);
        }
        log.info("批量发送消息成功");
    }

    /**
     * 示例7：发送 JSON 消息
     */
    public void sendJsonMessage() throws MqttException {
        String json = "{\"deviceId\":\"device001\",\"temperature\":25.5,\"humidity\":60}";
        mqttTemplate.send("testtopic/json", json, 1);
        log.info("发送 JSON 消息成功");
    }
}
