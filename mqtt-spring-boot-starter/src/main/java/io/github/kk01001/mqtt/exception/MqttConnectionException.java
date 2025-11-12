package io.github.kk01001.mqtt.exception;

/**
 * MQTT 连接异常
 *
 * @author kk01001
 */
public class MqttConnectionException extends RuntimeException {

    public MqttConnectionException(String message) {
        super(message);
    }

    public MqttConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
