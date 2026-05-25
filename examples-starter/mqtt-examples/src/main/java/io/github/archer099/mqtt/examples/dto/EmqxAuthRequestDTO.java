package io.github.archer099.mqtt.examples.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author archer099
 * @date 2025-11-17 15:45:00
 * @description EMQX HTTP 鉴权请求 DTO
 */
@Data
@Schema(description = "EMQX HTTP 鉴权请求")
public class EmqxAuthRequestDTO {

    @Schema(description = "MQTT ClientID")
    private String clientid;

    @NotBlank
    @Schema(description = "用户名")
    private String username;

    @NotBlank
    @Schema(description = "密码")
    private String password;

    @Schema(description = "客户端 IP")
    private String peerhost;

    @Schema(description = "TLS 证书 CommonName")
    private String cert_common_name;
}