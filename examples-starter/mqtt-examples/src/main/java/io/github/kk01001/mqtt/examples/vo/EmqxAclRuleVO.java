package io.github.kk01001.mqtt.examples.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author kk01001
 * @date 2025-11-17 16:02:00
 * @description EMQX HTTP 鉴权响应中的 ACL 规则
 */
@Data
@Schema(description = "EMQX ACL 规则")
public class EmqxAclRuleVO {

    @Schema(description = "权限 allow/deny")
    private String permission;

    @Schema(description = "动作 subscribe/publish")
    private String action;

    @Schema(description = "允许或拒绝的主题列表")
    private List<String> topics;
}