package io.github.archer099.mqtt.examples.controller;

import io.github.archer099.mqtt.examples.dto.EmqxAuthRequestDTO;
import io.github.archer099.mqtt.examples.service.EmqxAuthService;
import io.github.archer099.mqtt.examples.vo.EmqxAuthResponseVO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author archer099
 * @date 2025-11-17 16:02:00
 * @description EMQX HTTP 鉴权接口
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/emqx")
@Slf4j
public class EmqxAuthController {

    private final EmqxAuthService emqxAuthService;

    /**
     * 密码鉴权接口（EMQX HTTP）
     */
    @PostMapping(value = "/auth", produces = "application/json")
    @Schema(description = "EMQX 密码鉴权")
    public EmqxAuthResponseVO auth(@Valid @RequestBody EmqxAuthRequestDTO request) {
        log.info("[EMQX AUTH] request: {}", request);
        EmqxAuthResponseVO resp = emqxAuthService.authenticate(request);
        log.info("[EMQX AUTH] response: {}", resp);
        return resp;
    }
}