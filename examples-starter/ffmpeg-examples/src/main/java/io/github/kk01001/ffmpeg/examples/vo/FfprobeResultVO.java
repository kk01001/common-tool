package io.github.archer099.ffmpeg.examples.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author archer099
 * @date 2025-11-17 16:30:00
 * @description ffprobe 结果
 */
@Data
public class FfprobeResultVO {

    @Schema(description = "原始 JSON 字符串")
    private String rawJson;
}