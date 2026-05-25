package io.github.archer099.ffmpeg.examples.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * @author archer099
 * @date 2025-11-17 16:30:00
 * @description 音频转 HLS 入参
 */
@Data
public class AudioHlsConvertDTO {

    @Schema(description = "输入文件路径 (mp3/aac 等)")
    @NotBlank(message = "inputPath 不能为空")
    private String inputPath;

    @Schema(description = "输出目录路径，存放 m3u8 与分片")
    @NotBlank(message = "outputDir 不能为空")
    private String outputDir;

    @Schema(description = "分片类型 ts 或 fmp4，默认 fmp4")
    @Pattern(regexp = "(?i)ts|fmp4", message = "segmentType 必须为 ts 或 fmp4")
    private String segmentType;

    @Schema(description = "分片时长秒，默认 6")
    @Positive(message = "hlsTime 必须为正数")
    private Integer hlsTime;

    @Schema(description = "音频码率，如 128k")
    private String audioBitrate;

    @Schema(description = "采样率，如 48000")
    @Positive(message = "sampleRate 必须为正数")
    private Integer sampleRate;
}