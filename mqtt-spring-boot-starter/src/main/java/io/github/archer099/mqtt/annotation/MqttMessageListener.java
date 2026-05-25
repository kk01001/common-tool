package io.github.archer099.mqtt.annotation;

import java.lang.annotation.*;

/**
 * MQTT 消息监听器注解
 * 标注在方法上，用于接收 MQTT 消息
 *
 * @author archer099
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MqttMessageListener {

    /**
     * 订阅的主题，支持通配符
     * 单层通配符：+
     * 多层通配符：#
     * 例如：testtopic/+/test 或 testtopic/#
     */
    String[] topics();

    /**
     * QoS 级别
     * 0：最多一次
     * 1：至少一次
     * 2：只有一次
     */
    int qos() default 1;
}
