package io.github.archer099.mqtt.examples.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author archer099
 * @date 2025-11-17 11:08:00
 * @description MQTT 批量发送结果
 */
@Data
@Schema(description = "MQTT 批量发送结果")
public class MqttBulkSendResultVO {

    @Schema(description = "主题")
    private String topic;

    @Schema(description = "请求条数")
    private Integer count;

    @Schema(description = "QoS 等级")
    private Integer qos;

    @Schema(description = "成功条数")
    private Integer successCount;

    @Schema(description = "失败条数")
    private Integer failureCount;

    @Schema(description = "耗时毫秒")
    private Long durationMs;
}