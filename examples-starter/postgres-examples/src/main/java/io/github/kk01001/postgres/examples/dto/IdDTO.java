package io.github.kk01001.postgres.examples.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class IdDTO {

    @Schema(description = "主键ID")
    @NotNull
    private Long id;
}