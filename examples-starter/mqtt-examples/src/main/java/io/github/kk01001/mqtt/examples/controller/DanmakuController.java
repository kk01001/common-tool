package io.github.archer099.mqtt.examples.controller;

import io.github.archer099.common.model.ApiResponse;
import io.github.archer099.mqtt.examples.dto.DanmakuSendRequestDTO;
import io.github.archer099.mqtt.examples.service.DanmakuService;
import io.github.archer099.mqtt.examples.vo.DanmakuSendResponseVO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/danmaku")
public class DanmakuController {

    private final DanmakuService danmakuService;

    @PostMapping("/send")
    @Schema(description = "发送弹幕")
    public ApiResponse<DanmakuSendResponseVO> send(@Valid @RequestBody DanmakuSendRequestDTO request) {
        DanmakuSendResponseVO result = danmakuService.send(request);
        return ApiResponse.ok(result);
    }
}