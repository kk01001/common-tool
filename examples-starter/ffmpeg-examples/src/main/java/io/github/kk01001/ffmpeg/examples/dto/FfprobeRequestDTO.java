package io.github.kk01001.ffmpeg.examples.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author kk01001
 * @date 2025-11-17 16:30:00
 * @description ffprobe 请求参数
 */
@Data
public class FfprobeRequestDTO {

    @Schema(description = "输入文件路径")
    @NotBlank(message = "inputPath 不能为空")
    private String inputPath;
}