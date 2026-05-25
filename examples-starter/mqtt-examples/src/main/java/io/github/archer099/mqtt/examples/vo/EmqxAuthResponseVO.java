package io.github.archer099.mqtt.examples.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author archer099
 * @date 2025-11-17 16:02:00
 * @description EMQX HTTP 鉴权响应 VO
 */
@Data
@Schema(description = "EMQX HTTP 鉴权响应")
public class EmqxAuthResponseVO {

    @Schema(description = "鉴权结果 allow/deny/ignore")
    private String result;

    @Schema(description = "是否超级用户（可选）")
    private Boolean is_superuser;

    @Schema(description = "客户端属性（可选）")
    private Map<String, String> client_attrs;

    @Schema(description = "鉴权过期时间（Unix 秒，可选）")
    private Long expire_at;

    @Schema(description = "ACL 规则列表（可选）")
    private List<EmqxAclRuleVO> acl;
}