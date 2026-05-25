package io.github.archer099.ffmpeg;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author archer099
 * @date 2025-11-17 16:22:00
 * @description FFmpeg 属性配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "ffmpeg")
public class FfmpegProperties {
    /** 是否启用 FFmpeg 功能 */
    private Boolean enabled = true;

    /** ffmpeg 可执行路径 */
    private String ffmpegPath = "ffmpeg";

    /** ffprobe 可执行路径 */
    private String ffprobePath = "ffprobe";

    /** 工作目录（输出相对路径基准） */
    private String workDir = "";

    /** 命令执行超时秒数 */
    private Integer timeoutSeconds = 300;

    /** HLS 分片时长秒 */
    private Integer hlsTime = 6;

    /** 音频码率，例如 128k */
    private String audioBitrate = "128k";

    /** 音频采样率，例如 48000 */
    private Integer sampleRate = 48000;

    /** 默认分片类型 ts 或 fmp4 */
    private String segmentType = "fmp4";
}