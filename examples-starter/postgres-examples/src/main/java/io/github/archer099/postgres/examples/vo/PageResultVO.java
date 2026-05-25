package io.github.archer099.postgres.examples.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class PageResultVO<T> {

    @Schema(description = "总条数")
    private Long total;

    @Schema(description = "当前页数据")
    private List<T> items;
}