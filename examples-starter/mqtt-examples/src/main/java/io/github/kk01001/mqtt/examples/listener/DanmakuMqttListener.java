package io.github.kk01001.mqtt.examples.listener;

import io.github.kk01001.mqtt.annotation.MqttMessageListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DanmakuMqttListener {

    @MqttMessageListener(topics = "$share/danmaku/video/+/danmaku", qos = 1)
    public void handle(String topic, String payload) {
        log.info("Share订阅弹幕, topic={}, payload={}", topic, payload);
    }
}