package io.github.archer099.mqtt.example;

import io.github.archer099.mqtt.core.MqttBroadcastTemplate;
import io.github.archer099.mqtt.core.MqttBroadcastTemplate.BatchSendResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * MQTT 广播和批量发送示例
 *
 * @author archer099
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MqttBroadcastExample {

    private final MqttBroadcastTemplate broadcastTemplate;

    /**
     * 示例1：广播消息到所有设备
     * 使用通配符主题，所有订阅该主题的设备都会收到消息
     */
    public void broadcastToAllDevices() throws Exception {
        // 广播到所有设备
        broadcastTemplate.broadcast("device/+/command", "系统升级通知");
        log.info("广播消息已发送");
    }

    /**
     * 示例2：批量发送相同消息到多个设备（同步）
     */
    public void sendSameMessageToDevices() {
        // 设备 ID 列表
        List<String> deviceIds = Arrays.asList(
                "device001", "device002", "device003", 
                "device004", "device005"
        );

        // 批量发送相同消息
        BatchSendResult result = broadcastTemplate.sendToDevices(
                deviceIds,
                "重启指令",
                "device/{deviceId}/command"  // 主题模板
        );

        // 打印结果
        log.info("批量发送结果: {}", result);
        log.info("成功设备: {}", result.getSuccessDevices());
        
        if (!result.isAllSuccess()) {
            log.warn("失败设备:");
            result.getFailures().forEach(failure -> 
                log.warn("  - {}", failure)
            );
        }
    }

    /**
     * 示例3：批量发送相同消息到多个设备（异步）
     */
    public void sendSameMessageToDevicesAsync() {
        List<String> deviceIds = Arrays.asList(
                "device001", "device002", "device003", 
                "device004", "device005", "device006",
                "device007", "device008", "device009", "device010"
        );

        // 异步批量发送
        CompletableFuture<BatchSendResult> future = broadcastTemplate.sendToDevicesAsync(
                deviceIds,
                "配置更新",
                "device/{deviceId}/config"
        );

        // 处理结果
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("批量发送异常", ex);
            } else {
                log.info("异步批量发送完成: {}", result);
                log.info("成功率: {}%", result.getSuccessRate());
            }
        });
    }

    /**
     * 示例4：发送不同消息到多个设备（同步）
     * 每个设备收到的消息内容不同
     */
    public void sendDifferentMessagesToDevices() {
        // 为每个设备准备不同的消息
        Map<String, String> messages = new HashMap<>();
        messages.put("device001", "{\"action\":\"restart\",\"delay\":10}");
        messages.put("device002", "{\"action\":\"update\",\"version\":\"1.2.0\"}");
        messages.put("device003", "{\"action\":\"config\",\"mode\":\"auto\"}");
        messages.put("device004", "{\"action\":\"report\",\"interval\":60}");
        messages.put("device005", "{\"action\":\"reset\",\"type\":\"soft\"}");

        // 批量发送不同消息
        BatchSendResult result = broadcastTemplate.sendDifferentMessages(
                messages,
                "device/{deviceId}/command"
        );

        log.info("发送不同消息结果: {}", result);
    }

    /**
     * 示例5：发送不同消息到多个设备（异步）
     */
    public void sendDifferentMessagesToDevicesAsync() {
        Map<String, String> messages = new HashMap<>();
        for (int i = 1; i <= 20; i++) {
            String deviceId = String.format("device%03d", i);
            String message = String.format("{\"deviceId\":\"%s\",\"command\":\"status\",\"timestamp\":%d}", 
                    deviceId, System.currentTimeMillis());
            messages.put(deviceId, message);
        }

        // 异步批量发送不同消息
        CompletableFuture<BatchSendResult> future = broadcastTemplate.sendDifferentMessagesAsync(
                messages,
                "device/{deviceId}/query"
        );

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("异步批量发送失败", ex);
            } else {
                log.info("异步发送不同消息完成: {}", result);
                log.info("总数: {}, 成功: {}, 失败: {}, 耗时: {}ms",
                        result.getTotalCount(),
                        result.getSuccessCount(),
                        result.getFailureCount(),
                        result.getDuration());
            }
        });
    }

    /**
     * 示例6：按区域广播消息
     */
    public void broadcastByRegion() throws Exception {
        // 华东区设备
        broadcastTemplate.broadcast("device/east/+/command", "华东区维护通知");
        
        // 华南区设备
        broadcastTemplate.broadcast("device/south/+/command", "华南区维护通知");
        
        // 华北区设备
        broadcastTemplate.broadcast("device/north/+/command", "华北区维护通知");
        
        log.info("按区域广播完成");
    }

    /**
     * 示例7：按设备类型批量发送
     */
    public void sendByDeviceType() {
        // 传感器设备
        List<String> sensors = Arrays.asList("sensor001", "sensor002", "sensor003");
        broadcastTemplate.sendToDevices(
                sensors,
                "{\"action\":\"calibrate\"}",
                "sensor/{deviceId}/command"
        );

        // 控制器设备
        List<String> controllers = Arrays.asList("ctrl001", "ctrl002", "ctrl003");
        broadcastTemplate.sendToDevices(
                controllers,
                "{\"action\":\"sync\"}",
                "controller/{deviceId}/command"
        );

        log.info("按设备类型批量发送完成");
    }

    /**
     * 示例8：大批量设备发送（1000+ 设备）
     */
    public void sendToMassiveDevices() {
        // 生成大量设备 ID
        List<String> deviceIds = new ArrayList<>();
        for (int i = 1; i <= 1000; i++) {
            deviceIds.add(String.format("device%04d", i));
        }

        log.info("开始向 {} 个设备发送消息...", deviceIds.size());

        // 使用异步批量发送提高性能
        CompletableFuture<BatchSendResult> future = broadcastTemplate.sendToDevicesAsync(
                deviceIds,
                "批量升级通知",
                "device/{deviceId}/upgrade",
                1  // QoS 1
        );

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("大批量发送失败", ex);
            } else {
                log.info("大批量发送完成:");
                log.info("  - 总设备数: {}", result.getTotalCount());
                log.info("  - 成功数: {}", result.getSuccessCount());
                log.info("  - 失败数: {}", result.getFailureCount());
                log.info("  - 成功率: {:.2f}%", result.getSuccessRate());
                log.info("  - 总耗时: {}ms", result.getDuration());
                log.info("  - 平均耗时: {:.2f}ms/设备", 
                        (double) result.getDuration() / result.getTotalCount());
            }
        });
    }

    /**
     * 示例9：分批发送（避免一次性发送过多）
     */
    public void sendInBatches() {
        // 所有设备
        List<String> allDevices = new ArrayList<>();
        for (int i = 1; i <= 500; i++) {
            allDevices.add(String.format("device%03d", i));
        }

        // 分批发送，每批 50 个设备
        int batchSize = 50;
        int totalBatches = (allDevices.size() + batchSize - 1) / batchSize;

        log.info("开始分批发送，总设备数: {}, 批次数: {}", allDevices.size(), totalBatches);

        for (int i = 0; i < totalBatches; i++) {
            int start = i * batchSize;
            int end = Math.min(start + batchSize, allDevices.size());
            List<String> batch = allDevices.subList(start, end);

            log.info("发送第 {}/{} 批，设备数: {}", i + 1, totalBatches, batch.size());

            BatchSendResult result = broadcastTemplate.sendToDevices(
                    batch,
                    "分批通知",
                    "device/{deviceId}/notification"
            );

            log.info("第 {} 批发送完成: {}", i + 1, result);

            // 批次间延迟，避免过载
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        log.info("分批发送全部完成");
    }
}
