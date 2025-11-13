package io.github.kk01001.mqtt;

import io.github.kk01001.mqtt.core.MqttTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * MQTT 测试应用
 *
 * @author kk01001
 */
@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
public class MqttTestApplication implements CommandLineRunner {

    private final MqttTemplate mqttTemplate;

    public static void main(String[] args) {
        SpringApplication.run(MqttTestApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("MQTT 测试应用启动成功");

        // 等待连接建立
        Thread.sleep(2000);

        // 测试发送消息
        testSendMessages();

        // 保持应用运行以接收消息
        log.info("应用正在运行，等待接收消息...");
    }

    private void testSendMessages() throws MqttException, InterruptedException {
        log.info("开始测试发送消息...");

        // 1. 发送简单消息
        mqttTemplate.send("testtopic/1", "Hello MQTT!");
        log.info("发送消息到 testtopic/1");
        Thread.sleep(500);

        // 2. 发送带 QoS 的消息
        mqttTemplate.send("testtopic/2", "Hello with QoS 2", 2);
        log.info("发送消息到 testtopic/2 (QoS 2)");
        Thread.sleep(500);

        // 3. 发送保留消息
        mqttTemplate.send("testtopic/3", "Retained message", 1, true);
        log.info("发送保留消息到 testtopic/3");
        Thread.sleep(500);

        // 4. 发送 JSON 消息
        String json = "{\"deviceId\":\"device001\",\"temperature\":25.5,\"humidity\":60}";
        mqttTemplate.send("testtopic/json", json, 1);
        log.info("发送 JSON 消息到 testtopic/json");
        Thread.sleep(500);

        // 5. 异步发送消息
        mqttTemplate.sendAsync("testtopic/async", "Async message")
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("异步发送失败", ex);
                    } else {
                        log.info("异步发送成功到 testtopic/async");
                    }
                });
        Thread.sleep(500);

        // 6. 批量发送消息
        for (int i = 0; i < 5000; i++) {
            try {
                mqttTemplate.send("demo/topic", "Batch message " + i, 2);
                // TimeUnit.MILLISECONDS.sleep(100);
            } catch (MqttException e) {
                log.error("批量发送消息失败", e);
            }
        }
        log.info("批量发送 5 条消息到 demo/topic");

        log.info("消息发送测试完成");
    }
}
