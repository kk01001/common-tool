package io.github.archer099.postgres.examples.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class PageQueryDTO {

    @Schema(description = "页码")
    @Min(1)
    private Integer pageNumber = 1;

    @Schema(description = "每页条数")
    @Min(1)
    private Integer pageSize = 10;

    @Schema(description = "关键词")
    private String keyword;
}