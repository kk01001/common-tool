package io.github.archer099.mqtt.config;

import io.github.archer099.mqtt.core.MqttBroadcastTemplate;
import io.github.archer099.mqtt.core.MqttClientManager;
import io.github.archer099.mqtt.core.MqttListenerAnnotationBeanPostProcessor;
import io.github.archer099.mqtt.core.MqttTemplate;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * MQTT 自动配置类
 *
 * @author archer099
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(MqttProperties.class)
@ConditionalOnProperty(prefix = "mqtt", name = "enabled", havingValue = "true")
public class MqttAutoConfiguration {

    private final MqttProperties mqttProperties;

    /**
     * 创建 MQTT 客户端管理器
     */
    @Bean
    @ConditionalOnMissingBean
    public MqttClientManager mqttClientManager() throws MqttException {
        log.info("初始化 MQTT 客户端管理器");
        MqttClientManager manager = new MqttClientManager(mqttProperties);
        manager.connect();
        return manager;
    }

    /**
     * 创建 MQTT 消息发送模板
     */
    @Bean
    @ConditionalOnMissingBean
    public MqttTemplate mqttTemplate(MqttClientManager mqttClientManager) {
        log.info("初始化 MQTT 消息发送模板");
        return new MqttTemplate(mqttClientManager, mqttProperties);
    }

    /**
     * 创建 MQTT 广播和批量发送模板
     */
    @Bean
    @ConditionalOnMissingBean
    public MqttBroadcastTemplate mqttBroadcastTemplate(MqttTemplate mqttTemplate) {
        log.info("初始化 MQTT 广播和批量发送模板");
        return new MqttBroadcastTemplate(mqttTemplate, mqttProperties);
    }

    /**
     * 创建消息消费线程池
     */
    @Bean
    @ConditionalOnMissingBean(name = "mqttConsumerExecutor")
    public ExecutorService mqttConsumerExecutor() {
        int threadPoolSize = mqttProperties.getConsumer().getThreadPoolSize();
        int queueCapacity = mqttProperties.getConsumer().getQueueCapacity();

        log.info("初始化 MQTT 消费者线程池: corePoolSize={}, maxPoolSize={}, queueCapacity={}", 
                threadPoolSize, threadPoolSize * 2, queueCapacity);

        return new ThreadPoolExecutor(
                threadPoolSize,
                threadPoolSize * 2,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(queueCapacity),
                r -> {
                    Thread thread = new Thread(r);
                    thread.setName("mqtt-consumer-" + thread.getId());
                    thread.setDaemon(true);
                    return thread;
                },
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    /**
     * 创建 MQTT 监听器注解处理器
     */
    @Bean
    @ConditionalOnMissingBean
    public MqttListenerAnnotationBeanPostProcessor mqttListenerAnnotationBeanPostProcessor(
            MqttClientManager mqttClientManager,
            ExecutorService mqttConsumerExecutor) {
        log.info("初始化 MQTT 监听器注解处理器");
        return new MqttListenerAnnotationBeanPostProcessor(mqttClientManager, mqttConsumerExecutor);
    }

    /**
     * 销毁时关闭 MQTT 连接
     */
    @PreDestroy
    public void destroy() {
        log.info("关闭 MQTT 连接");
        try {
            MqttClientManager manager = mqttClientManager();
            if (manager != null) {
                manager.close();
            }
        } catch (Exception e) {
            log.error("关闭 MQTT 连接失败", e);
        }
    }
}
