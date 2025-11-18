package io.github.kk01001.mqtt.examples.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "弹幕发送结果")
public class DanmakuSendResponseVO {

    @Schema(description = "主题")
    private String topic;

    @Schema(description = "QoS 等级")
    private Integer qos;

    @Schema(description = "消息内容")
    private String payload;
}