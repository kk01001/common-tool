package io.github.kk01001.postgres.examples.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserDTO {

    @Schema(description = "用户ID")
    @NotNull
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "邮箱")
    @Email
    private String email;
}