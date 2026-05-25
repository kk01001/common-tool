package io.github.archer099.mqtt.interceptor;

import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * MQTT 消息拦截器接口
 * 可用于消息发送前的预处理、日志记录、监控等
 *
 * @author archer099
 */
public interface MqttMessageInterceptor {

    /**
     * 发送消息前拦截
     *
     * @param topic   主题
     * @param message 消息
     * @return true 继续发送，false 拦截不发送
     */
    default boolean beforeSend(String topic, MqttMessage message) {
        return true;
    }

    /**
     * 发送消息后拦截
     *
     * @param topic   主题
     * @param message 消息
     * @param success 是否发送成功
     */
    default void afterSend(String topic, MqttMessage message, boolean success) {
    }

    /**
     * 接收消息前拦截
     *
     * @param topic   主题
     * @param message 消息
     * @return true 继续处理，false 拦截不处理
     */
    default boolean beforeReceive(String topic, MqttMessage message) {
        return true;
    }

    /**
     * 接收消息后拦截
     *
     * @param topic   主题
     * @param message 消息
     * @param success 是否处理成功
     */
    default void afterReceive(String topic, MqttMessage message, boolean success) {
    }

    /**
     * 获取拦截器优先级（数字越小优先级越高）
     */
    default int getOrder() {
        return 0;
    }
}
