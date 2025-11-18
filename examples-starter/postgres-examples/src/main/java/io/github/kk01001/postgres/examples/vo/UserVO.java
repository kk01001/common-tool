package io.github.kk01001.postgres.examples.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class UserVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;
}