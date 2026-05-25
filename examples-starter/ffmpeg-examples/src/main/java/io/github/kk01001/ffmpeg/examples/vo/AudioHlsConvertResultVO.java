package io.github.archer099.ffmpeg.examples.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author archer099
 * @date 2025-11-17 16:30:00
 * @description 音频转 HLS 结果
 */
@Data
public class AudioHlsConvertResultVO {

    @Schema(description = "m3u8 清单相对/绝对路径")
    private String m3u8Path;

    @Schema(description = "分片目录")
    private String segmentDir;

    @Schema(description = "分片数量（估计值）")
    private Integer segmentCount;

    @Schema(description = "执行耗时毫秒")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long durationMs;

    @Schema(description = "服务端保存的输入文件绝对路径")
    private String inputPath;
}