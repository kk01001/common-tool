package io.github.archer099.mqtt.core;

import io.github.archer099.mqtt.config.MqttProperties;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * MQTT 广播和批量发送模板
 * 支持向多个设备同时发送消息
 *
 * @author archer099
 */
@Slf4j
public class MqttBroadcastTemplate {

    private final MqttTemplate mqttTemplate;
    private final MqttProperties properties;
    private final ExecutorService broadcastExecutor;

    public MqttBroadcastTemplate(MqttTemplate mqttTemplate, MqttProperties properties) {
        this.mqttTemplate = mqttTemplate;
        this.properties = properties;
        // 创建专用的广播线程池
        this.broadcastExecutor = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors() * 2,
                r -> {
                    Thread thread = new Thread(r);
                    thread.setName("mqtt-broadcast-" + thread.getId());
                    thread.setDaemon(true);
                    return thread;
                }
        );
    }

    /**
     * 广播消息到所有设备（使用通配符主题）
     * 例如：broadcast("device/#", "message") 会发送到所有 device/ 开头的主题
     *
     * @param topicPattern 主题模式（支持通配符）
     * @param payload      消息内容
     */
    public void broadcast(String topicPattern, String payload) throws MqttException {
        broadcast(topicPattern, payload, properties.getProducer().getDefaultQos());
    }

    /**
     * 广播消息到所有设备（指定 QoS）
     *
     * @param topicPattern 主题模式
     * @param payload      消息内容
     * @param qos          QoS 级别
     */
    public void broadcast(String topicPattern, String payload, int qos) throws MqttException {
        log.info("广播消息到主题: {}, QoS: {}", topicPattern, qos);
        mqttTemplate.send(topicPattern, payload, qos);
    }

    /**
     * 批量发送消息到多个设备（同步）
     *
     * @param deviceIds 设备 ID 列表
     * @param payload   消息内容
     * @param topicTemplate 主题模板，使用 {deviceId} 作为占位符，例如：device/{deviceId}/command
     */
    public BatchSendResult sendToDevices(Collection<String> deviceIds, String payload, String topicTemplate) {
        return sendToDevices(deviceIds, payload, topicTemplate, properties.getProducer().getDefaultQos());
    }

    /**
     * 批量发送消息到多个设备（同步，指定 QoS）
     *
     * @param deviceIds     设备 ID 列表
     * @param payload       消息内容
     * @param topicTemplate 主题模板
     * @param qos           QoS 级别
     */
    public BatchSendResult sendToDevices(Collection<String> deviceIds, String payload, String topicTemplate, int qos) {
        log.info("批量发送消息到 {} 个设备", deviceIds.size());
        
        BatchSendResult result = new BatchSendResult(deviceIds.size());
        long startTime = System.currentTimeMillis();

        for (String deviceId : deviceIds) {
            String topic = topicTemplate.replace("{deviceId}", deviceId);
            try {
                mqttTemplate.send(topic, payload, qos);
                result.addSuccess(deviceId, topic);
            } catch (Exception e) {
                log.error("发送消息到设备 {} 失败: {}", deviceId, e.getMessage());
                result.addFailure(deviceId, topic, e.getMessage());
            }
        }

        result.setDuration(System.currentTimeMillis() - startTime);
        log.info("批量发送完成: 成功 {}, 失败 {}, 耗时 {}ms", 
                result.getSuccessCount(), result.getFailureCount(), result.getDuration());
        
        return result;
    }

    /**
     * 批量发送消息到多个设备（异步并行）
     *
     * @param deviceIds     设备 ID 列表
     * @param payload       消息内容
     * @param topicTemplate 主题模板
     */
    public CompletableFuture<BatchSendResult> sendToDevicesAsync(Collection<String> deviceIds, String payload, String topicTemplate) {
        return sendToDevicesAsync(deviceIds, payload, topicTemplate, properties.getProducer().getDefaultQos());
    }

    /**
     * 批量发送消息到多个设备（异步并行，指定 QoS）
     *
     * @param deviceIds     设备 ID 列表
     * @param payload       消息内容
     * @param topicTemplate 主题模板
     * @param qos           QoS 级别
     */
    public CompletableFuture<BatchSendResult> sendToDevicesAsync(Collection<String> deviceIds, String payload, String topicTemplate, int qos) {
        log.info("异步批量发送消息到 {} 个设备", deviceIds.size());
        
        return CompletableFuture.supplyAsync(() -> {
            BatchSendResult result = new BatchSendResult(deviceIds.size());
            long startTime = System.currentTimeMillis();

            // 并行发送
            List<CompletableFuture<Void>> futures = deviceIds.stream()
                    .map(deviceId -> {
                        String topic = topicTemplate.replace("{deviceId}", deviceId);
                        return mqttTemplate.sendAsync(topic, payload, qos)
                                .thenAccept(v -> result.addSuccess(deviceId, topic))
                                .exceptionally(ex -> {
                                    log.error("发送消息到设备 {} 失败: {}", deviceId, ex.getMessage());
                                    result.addFailure(deviceId, topic, ex.getMessage());
                                    return null;
                                });
                    })
                    .collect(Collectors.toList());

            // 等待所有发送完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            result.setDuration(System.currentTimeMillis() - startTime);
            log.info("异步批量发送完成: 成功 {}, 失败 {}, 耗时 {}ms",
                    result.getSuccessCount(), result.getFailureCount(), result.getDuration());
            
            return result;
        }, broadcastExecutor);
    }

    /**
     * 发送不同消息到多个设备（同步）
     *
     * @param messages      设备消息映射 (deviceId -> message)
     * @param topicTemplate 主题模板
     */
    public BatchSendResult sendDifferentMessages(java.util.Map<String, String> messages, String topicTemplate) {
        return sendDifferentMessages(messages, topicTemplate, properties.getProducer().getDefaultQos());
    }

    /**
     * 发送不同消息到多个设备（同步，指定 QoS）
     *
     * @param messages      设备消息映射 (deviceId -> message)
     * @param topicTemplate 主题模板
     * @param qos           QoS 级别
     */
    public BatchSendResult sendDifferentMessages(java.util.Map<String, String> messages, String topicTemplate, int qos) {
        log.info("批量发送不同消息到 {} 个设备", messages.size());
        
        BatchSendResult result = new BatchSendResult(messages.size());
        long startTime = System.currentTimeMillis();

        for (java.util.Map.Entry<String, String> entry : messages.entrySet()) {
            String deviceId = entry.getKey();
            String payload = entry.getValue();
            String topic = topicTemplate.replace("{deviceId}", deviceId);
            
            try {
                mqttTemplate.send(topic, payload, qos);
                result.addSuccess(deviceId, topic);
            } catch (Exception e) {
                log.error("发送消息到设备 {} 失败: {}", deviceId, e.getMessage());
                result.addFailure(deviceId, topic, e.getMessage());
            }
        }

        result.setDuration(System.currentTimeMillis() - startTime);
        log.info("批量发送完成: 成功 {}, 失败 {}, 耗时 {}ms",
                result.getSuccessCount(), result.getFailureCount(), result.getDuration());
        
        return result;
    }

    /**
     * 发送不同消息到多个设备（异步并行）
     *
     * @param messages      设备消息映射 (deviceId -> message)
     * @param topicTemplate 主题模板
     */
    public CompletableFuture<BatchSendResult> sendDifferentMessagesAsync(java.util.Map<String, String> messages, String topicTemplate) {
        return sendDifferentMessagesAsync(messages, topicTemplate, properties.getProducer().getDefaultQos());
    }

    /**
     * 发送不同消息到多个设备（异步并行，指定 QoS）
     *
     * @param messages      设备消息映射 (deviceId -> message)
     * @param topicTemplate 主题模板
     * @param qos           QoS 级别
     */
    public CompletableFuture<BatchSendResult> sendDifferentMessagesAsync(java.util.Map<String, String> messages, String topicTemplate, int qos) {
        log.info("异步批量发送不同消息到 {} 个设备", messages.size());
        
        return CompletableFuture.supplyAsync(() -> {
            BatchSendResult result = new BatchSendResult(messages.size());
            long startTime = System.currentTimeMillis();

            // 并行发送
            List<CompletableFuture<Void>> futures = messages.entrySet().stream()
                    .map(entry -> {
                        String deviceId = entry.getKey();
                        String payload = entry.getValue();
                        String topic = topicTemplate.replace("{deviceId}", deviceId);
                        
                        return mqttTemplate.sendAsync(topic, payload, qos)
                                .thenAccept(v -> result.addSuccess(deviceId, topic))
                                .exceptionally(ex -> {
                                    log.error("发送消息到设备 {} 失败: {}", deviceId, ex.getMessage());
                                    result.addFailure(deviceId, topic, ex.getMessage());
                                    return null;
                                });
                    })
                    .collect(Collectors.toList());

            // 等待所有发送完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            result.setDuration(System.currentTimeMillis() - startTime);
            log.info("异步批量发送完成: 成功 {}, 失败 {}, 耗时 {}ms",
                    result.getSuccessCount(), result.getFailureCount(), result.getDuration());
            
            return result;
        }, broadcastExecutor);
    }

    /**
     * 关闭广播线程池
     */
    public void shutdown() {
        if (broadcastExecutor != null && !broadcastExecutor.isShutdown()) {
            broadcastExecutor.shutdown();
            log.info("广播线程池已关闭");
        }
    }

    /**
     * 批量发送结果
     */
    public static class BatchSendResult {
        private final int totalCount;
        private final List<String> successDevices = new java.util.concurrent.CopyOnWriteArrayList<>();
        private final List<FailureInfo> failures = new java.util.concurrent.CopyOnWriteArrayList<>();
        private long duration;

        public BatchSendResult(int totalCount) {
            this.totalCount = totalCount;
        }

        public void addSuccess(String deviceId, String topic) {
            successDevices.add(deviceId);
        }

        public void addFailure(String deviceId, String topic, String error) {
            failures.add(new FailureInfo(deviceId, topic, error));
        }

        public int getTotalCount() {
            return totalCount;
        }

        public int getSuccessCount() {
            return successDevices.size();
        }

        public int getFailureCount() {
            return failures.size();
        }

        public List<String> getSuccessDevices() {
            return successDevices;
        }

        public List<FailureInfo> getFailures() {
            return failures;
        }

        public long getDuration() {
            return duration;
        }

        public void setDuration(long duration) {
            this.duration = duration;
        }

        public boolean isAllSuccess() {
            return failures.isEmpty();
        }

        public double getSuccessRate() {
            return totalCount == 0 ? 0 : (double) getSuccessCount() / totalCount * 100;
        }

        @Override
        public String toString() {
            return String.format("BatchSendResult{total=%d, success=%d, failure=%d, successRate=%.2f%%, duration=%dms}",
                    totalCount, getSuccessCount(), getFailureCount(), getSuccessRate(), duration);
        }
    }

    /**
     * 失败信息
     */
    public static class FailureInfo {
        private final String deviceId;
        private final String topic;
        private final String error;

        public FailureInfo(String deviceId, String topic, String error) {
            this.deviceId = deviceId;
            this.topic = topic;
            this.error = error;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public String getTopic() {
            return topic;
        }

        public String getError() {
            return error;
        }

        @Override
        public String toString() {
            return String.format("FailureInfo{deviceId='%s', topic='%s', error='%s'}", deviceId, topic, error);
        }
    }
}
