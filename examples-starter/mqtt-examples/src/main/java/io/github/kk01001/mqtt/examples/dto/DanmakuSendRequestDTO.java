package io.github.archer099.mqtt.examples.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "弹幕发送请求")
public class DanmakuSendRequestDTO {

    @NotBlank
    @Schema(description = "视频ID")
    private String videoId;

    @NotBlank
    @Schema(description = "弹幕内容")
    private String text;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "颜色，十六进制或标准色名")
    private String color;

    @Schema(description = "字号")
    private Integer fontSize;
}