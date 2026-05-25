package io.github.archer099.mqtt.core;

import io.github.archer099.mqtt.config.MqttProperties;
import io.github.archer099.mqtt.config.MqttSslProperties;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.util.StringUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.KeyManagerFactory;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * MQTT 客户端管理器
 * 负责创建、管理和维护 MQTT 客户端连接
 *
 * @author archer099
 */
@Slf4j
public class MqttClientManager {

    private final MqttProperties properties;
    private MqttClient mqttClient;
    private final Lock lock = new ReentrantLock();
    private final ConcurrentHashMap<String, IMqttMessageListener> topicListeners = new ConcurrentHashMap<>();

    public MqttClientManager(MqttProperties properties) {
        this.properties = properties;
    }

    /**
     * 初始化并连接 MQTT 客户端
     */
    public void connect() throws MqttException {
        lock.lock();
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                log.info("MQTT 客户端已连接");
                return;
            }

            String clientId = StringUtils.hasText(properties.getClientId())
                    ? properties.getClientId()
                    : "mqtt-client-" + UUID.randomUUID().toString();

            log.info("开始连接 MQTT Broker: {}, ClientId: {}", properties.getBrokerUrl(), clientId);

            mqttClient = new MqttClient(properties.getBrokerUrl(), clientId, new MemoryPersistence());

            MqttConnectOptions options = buildConnectOptions();
            mqttClient.setCallback(new DefaultMqttCallback());
            mqttClient.connect(options);

            log.info("MQTT 客户端连接成功");
        } finally {
            lock.unlock();
        }
    }

    /**
     * 构建连接选项
     */
    private MqttConnectOptions buildConnectOptions() {
        MqttConnectOptions options = new MqttConnectOptions();

        // 设置用户名和密码
        if (StringUtils.hasText(properties.getUsername())) {
            options.setUserName(properties.getUsername());
        }
        if (StringUtils.hasText(properties.getPassword())) {
            options.setPassword(properties.getPassword().toCharArray());
        }

        // 设置清除会话
        options.setCleanSession(properties.getCleanSession());

        // 设置连接超时
        options.setConnectionTimeout((int) properties.getConnectionTimeout().getSeconds());

        // 设置保持连接时间间隔
        options.setKeepAliveInterval((int) properties.getKeepAliveInterval().getSeconds());

        // 设置自动重连
        options.setAutomaticReconnect(properties.getAutomaticReconnect());

        // 设置最大重连延迟
        options.setMaxReconnectDelay((int) properties.getMaxReconnectDelay().toMillis());

        // 设置遗嘱消息
        if (properties.getWill() != null && StringUtils.hasText(properties.getWill().getTopic())) {
            MqttProperties.WillMessage will = properties.getWill();
            options.setWill(will.getTopic(),
                    will.getPayload().getBytes(),
                    will.getQos(),
                    will.getRetained());
        }

        // 设置 SSL/TLS
        if (properties.getSsl() != null && properties.getSsl().getEnabled()) {
            try {
                SSLSocketFactory socketFactory = createSslSocketFactory(properties.getSsl());
                options.setSocketFactory(socketFactory);
                log.info("SSL/TLS 已启用");
            } catch (Exception e) {
                log.error("配置 SSL/TLS 失败", e);
                throw new RuntimeException("配置 SSL/TLS 失败", e);
            }
        }

        return options;
    }

    /**
     * 创建 SSL Socket Factory
     */
    private SSLSocketFactory createSslSocketFactory(MqttSslProperties ssl) throws Exception {
        SSLContext sslContext = SSLContext.getInstance(ssl.getProtocol());

        // 加载密钥库
        KeyManagerFactory kmf = null;
        if (StringUtils.hasText(ssl.getKeyStore())) {
            KeyStore keyStore = KeyStore.getInstance(ssl.getKeyStoreType());
            try (FileInputStream fis = new FileInputStream(ssl.getKeyStore())) {
                keyStore.load(fis, ssl.getKeyStorePassword().toCharArray());
            }
            kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, ssl.getKeyStorePassword().toCharArray());
        }

        // 加载信任库
        TrustManagerFactory tmf = null;
        if (StringUtils.hasText(ssl.getTrustStore())) {
            KeyStore trustStore = KeyStore.getInstance(ssl.getTrustStoreType());
            try (FileInputStream fis = new FileInputStream(ssl.getTrustStore())) {
                trustStore.load(fis, ssl.getTrustStorePassword().toCharArray());
            }
            tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);
        }

        sslContext.init(
                kmf != null ? kmf.getKeyManagers() : null,
                tmf != null ? tmf.getTrustManagers() : null,
                null
        );

        return sslContext.getSocketFactory();
    }

    /**
     * 订阅主题
     */
    public void subscribe(String topic, int qos, IMqttMessageListener listener) throws MqttException {
        if (mqttClient == null || !mqttClient.isConnected()) {
            throw new IllegalStateException("MQTT 客户端未连接");
        }

        mqttClient.subscribe(topic, qos, listener);
        topicListeners.put(topic, listener);
        log.info("订阅主题成功: {}, QoS: {}", topic, qos);
    }

    /**
     * 取消订阅
     */
    public void unsubscribe(String topic) throws MqttException {
        if (mqttClient == null || !mqttClient.isConnected()) {
            throw new IllegalStateException("MQTT 客户端未连接");
        }

        mqttClient.unsubscribe(topic);
        topicListeners.remove(topic);
        log.info("取消订阅主题: {}", topic);
    }

    /**
     * 发布消息
     */
    public void publish(String topic, MqttMessage message) throws MqttException {
        if (mqttClient == null || !mqttClient.isConnected()) {
            throw new IllegalStateException("MQTT 客户端未连接");
        }

        mqttClient.publish(topic, message);
        log.debug("发布消息到主题: {}, QoS: {}, Retained: {}", topic, message.getQos(), message.isRetained());
    }

    /**
     * 异步发布消息
     */
    public IMqttDeliveryToken publishAsync(String topic, MqttMessage message, IMqttActionListener listener) throws MqttException {
        if (mqttClient == null || !mqttClient.isConnected()) {
            throw new IllegalStateException("MQTT 客户端未连接");
        }
        mqttClient.publish(topic, message);
        // if (listener != null) {
        //     token.setActionCallback(listener);
        // }
        log.debug("异步发布消息到主题: {}, QoS: {}, Retained: {}", topic, message.getQos(), message.isRetained());
        return null;
    }

    /**
     * 断开连接
     */
    public void disconnect() {
        lock.lock();
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnect();
                log.info("MQTT 客户端断开连接");
            }
        } catch (MqttException e) {
            log.error("断开 MQTT 连接失败", e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 关闭客户端
     */
    public void close() {
        lock.lock();
        try {
            if (mqttClient != null) {
                if (mqttClient.isConnected()) {
                    mqttClient.disconnect();
                }
                mqttClient.close();
                log.info("MQTT 客户端已关闭");
            }
        } catch (MqttException e) {
            log.error("关闭 MQTT 客户端失败", e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 检查是否已连接
     */
    public boolean isConnected() {
        return mqttClient != null && mqttClient.isConnected();
    }

    /**
     * 获取 MQTT 客户端
     */
    public MqttClient getMqttClient() {
        return mqttClient;
    }

    /**
     * 重新订阅所有主题（用于重连后）
     */
    private void resubscribeAll() {
        topicListeners.forEach((topic, listener) -> {
            try {
                mqttClient.subscribe(topic, properties.getConsumer().getDefaultQos(), listener);
                log.info("重新订阅主题: {}", topic);
            } catch (MqttException e) {
                log.error("重新订阅主题失败: {}", topic, e);
            }
        });
    }

    /**
     * 默认的 MQTT 回调处理器
     */
    private class DefaultMqttCallback implements MqttCallback {

        @Override
        public void connectionLost(Throwable cause) {
            log.warn("MQTT 连接丢失", cause);
            if (properties.getAutomaticReconnect()) {
                log.info("将自动重连...");
            }
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            // 消息由订阅时指定的 listener 处理
            log.debug("收到消息 - 主题: {}, QoS: {}, 内容: {}", topic, message.getQos(), new String(message.getPayload()));
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            try {
                log.debug("消息发送完成: {}", token.getMessage());
            } catch (Exception e) {
                log.error("获取已发送消息失败", e);
            }
        }

    }
}
