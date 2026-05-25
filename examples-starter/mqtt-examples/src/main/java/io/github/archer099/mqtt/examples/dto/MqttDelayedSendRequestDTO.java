package io.github.archer099.mqtt.examples.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "MQTT 延迟发布请求")
public class MqttDelayedSendRequestDTO {

    @NotBlank
    @Schema(description = "主题")
    private String topic;

    @NotNull
    @Min(1)
    @Schema(description = "延迟秒数")
    private Integer delaySeconds;

    @NotBlank
    @Schema(description = "消息内容")
    private String payload;
}