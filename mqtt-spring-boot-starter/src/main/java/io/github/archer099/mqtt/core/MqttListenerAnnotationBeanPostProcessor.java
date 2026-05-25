package io.github.archer099.mqtt.core;

import io.github.archer099.mqtt.annotation.MqttMessageListener;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;

/**
 * MQTT 监听器注解处理器
 * 扫描并注册带有 @MqttMessageListener 注解的方法
 *
 * @author archer099
 */
@Slf4j
public class MqttListenerAnnotationBeanPostProcessor implements BeanPostProcessor {

    private final MqttClientManager clientManager;
    private final ExecutorService executorService;

    public MqttListenerAnnotationBeanPostProcessor(MqttClientManager clientManager, ExecutorService executorService) {
        this.clientManager = clientManager;
        this.executorService = executorService;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);

        ReflectionUtils.doWithMethods(targetClass, method -> {
            MqttMessageListener annotation = AnnotationUtils.findAnnotation(method, MqttMessageListener.class);
            if (annotation != null) {
                registerListener(bean, method, annotation);
            }
        }, ReflectionUtils.USER_DECLARED_METHODS);

        return bean;
    }

    /**
     * 注册监听器
     */
    private void registerListener(Object bean, Method method, MqttMessageListener annotation) {
        String[] topics = annotation.topics();
        int qos = annotation.qos();

        for (String topic : topics) {
            try {
                IMqttMessageListener listener = (t, msg) -> {
                    executorService.submit(() -> {
                        try {
                            invokeListenerMethod(bean, method, t, msg);
                        } catch (Exception e) {
                            log.error("执行 MQTT 监听器方法失败: bean={}, method={}, topic={}", 
                                    bean.getClass().getSimpleName(), method.getName(), t, e);
                        }
                    });
                };

                clientManager.subscribe(topic, qos, listener);
                log.info("注册 MQTT 监听器: bean={}, method={}, topic={}, qos={}", 
                        bean.getClass().getSimpleName(), method.getName(), topic, qos);
            } catch (MqttException e) {
                log.error("订阅主题失败: topic={}, qos={}", topic, qos, e);
            }
        }
    }

    /**
     * 调用监听器方法
     */
    private void invokeListenerMethod(Object bean, Method method, String topic, MqttMessage message) throws Exception {
        Class<?>[] parameterTypes = method.getParameterTypes();
        Object[] args = new Object[parameterTypes.length];

        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> paramType = parameterTypes[i];

            if (paramType == String.class) {
                // 如果参数是 String 类型，根据位置判断是 topic 还是 payload
                if (i == 0) {
                    args[i] = topic;
                } else {
                    args[i] = new String(message.getPayload(), StandardCharsets.UTF_8);
                }
            } else if (paramType == byte[].class) {
                args[i] = message.getPayload();
            } else if (paramType == MqttMessage.class) {
                args[i] = message;
            } else if (paramType == int.class || paramType == Integer.class) {
                args[i] = message.getQos();
            } else if (paramType == boolean.class || paramType == Boolean.class) {
                args[i] = message.isRetained();
            } else {
                // 尝试将 payload 转换为字符串
                args[i] = new String(message.getPayload(), StandardCharsets.UTF_8);
            }
        }

        ReflectionUtils.makeAccessible(method);
        method.invoke(bean, args);
    }
}
