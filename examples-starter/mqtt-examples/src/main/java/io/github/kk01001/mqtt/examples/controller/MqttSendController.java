package io.github.kk01001.mqtt.examples.controller;

import io.github.kk01001.common.model.ApiResponse;
import io.github.kk01001.mqtt.examples.dto.MqttBulkSendRequestDTO;
import io.github.kk01001.mqtt.examples.service.MqttSendService;
import io.github.kk01001.mqtt.examples.vo.MqttBulkSendResultVO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author kk01001
 * @date 2025-11-17 11:08:00
 * @description MQTT 批量发送接口
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mqtt")
public class MqttSendController {

    private final MqttSendService mqttSendService;

    /**
     * 批量发送消息
     */
    @PostMapping("/bulk-send")
    @Schema(description = "批量发送消息")
    public ApiResponse<MqttBulkSendResultVO> bulkSend(@Valid @RequestBody MqttBulkSendRequestDTO request) {
        MqttBulkSendResultVO result = mqttSendService.bulkSend(request);
        return ApiResponse.ok(result);
    }
}