package io.github.archer099.mqtt.examples.controller;

import io.github.archer099.common.model.ApiResponse;
import io.github.archer099.mqtt.examples.dto.MqttDelayedSendRequestDTO;
import io.github.archer099.mqtt.examples.service.MqttDelayService;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mqtt/delay")
public class MqttDelayController {

    private final MqttDelayService mqttDelayService;

    @PostMapping("/send")
    @Schema(description = "延迟发布消息")
    public ApiResponse<Void> send(@Valid @RequestBody MqttDelayedSendRequestDTO request) {
        mqttDelayService.sendDelayed(request);
        return ApiResponse.ok();
    }
}