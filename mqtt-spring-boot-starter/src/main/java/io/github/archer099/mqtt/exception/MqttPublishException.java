package io.github.archer099.mqtt.exception;

/**
 * MQTT 消息发布异常
 *
 * @author archer099
 */
public class MqttPublishException extends RuntimeException {

    private final String topic;

    public MqttPublishException(String topic, String message) {
        super(message);
        this.topic = topic;
    }

    public MqttPublishException(String topic, String message, Throwable cause) {
        super(message, cause);
        this.topic = topic;
    }

    public String getTopic() {
        return topic;
    }
}
