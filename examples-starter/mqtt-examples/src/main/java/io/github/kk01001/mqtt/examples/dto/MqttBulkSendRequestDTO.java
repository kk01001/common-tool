package io.github.kk01001.mqtt.examples.dto;

import cn.hutool.core.util.StrUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Objects;

/**
 * @author kk01001
 * @date 2025-11-17 11:08:00
 * @description MQTT 批量发送请求参数
 */
@Data
@Schema(description = "MQTT 批量发送请求参数")
public class MqttBulkSendRequestDTO {

    @Schema(description = "主题")
    @NotBlank(message = "topic 不能为空")
    private String topic;

    @Schema(description = "发送条数")
    @NotNull(message = "count 不能为空")
    @Min(value = 1, message = "count 必须大于等于 1")
    private Integer count;

    @Schema(description = "QoS 等级，默认 2")
    private Integer qos = 2;

    @Schema(description = "消息前缀，默认 Batch message")
    private String payloadPrefix = "Batch message";

    public MqttBulkSendRequestDTO() {
    }

    public MqttBulkSendRequestDTO(String topic, Integer count, Integer qos, String payloadPrefix) {
        if (StrUtil.isBlank(topic)) {
            throw new IllegalArgumentException("topic 不能为空");
        }
        if (Objects.isNull(count) || count < 1) {
            throw new IllegalArgumentException("count 必须大于等于 1");
        }
        this.topic = topic;
        this.count = count;
        this.qos = Objects.isNull(qos) ? 2 : qos;
        this.payloadPrefix = StrUtil.isBlank(payloadPrefix) ? "Batch message" : payloadPrefix;
    }
}