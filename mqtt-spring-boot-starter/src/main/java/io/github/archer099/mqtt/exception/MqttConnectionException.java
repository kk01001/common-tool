package io.github.archer099.mqtt.exception;

/**
 * MQTT 连接异常
 *
 * @author archer099
 */
public class MqttConnectionException extends RuntimeException {

    public MqttConnectionException(String message) {
        super(message);
    }

    public MqttConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
