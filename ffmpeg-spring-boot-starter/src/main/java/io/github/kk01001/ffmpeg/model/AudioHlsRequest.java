package io.github.kk01001.ffmpeg.model;

import cn.hutool.core.util.StrUtil;
import lombok.Data;

import java.util.Objects;

/**
 * @author kk01001
 * @date 2025-11-17 16:22:00
 * @description 音频转 HLS 请求
 */
@Data
public class AudioHlsRequest {
    
    /** 
     * 输入文件路径 (mp3/aac 等)
     */
    private String inputPath;

    /** 
     * 输出目录路径，存放 m3u8 与分片 
     */
    private String outputDir;

    /** 
     * 分片类型 ts 或 fmp4 
     */
    private String segmentType;

    /** 
     * 分片时长（秒），默认 6 
     */
    private Integer hlsTime;

    /** 
     * 音频码率（如 128k） 
     */
    private String audioBitrate;

    /** 
     * 采样率（如 48000） 
     */
    private Integer sampleRate;

    public AudioHlsRequest() {
    }

    public AudioHlsRequest(String inputPath, String outputDir, String segmentType, Integer hlsTime,
                           String audioBitrate, Integer sampleRate) {
        if (StrUtil.isBlank(inputPath)) {
            throw new IllegalArgumentException("inputPath 不能为空");
        }
        if (StrUtil.isBlank(outputDir)) {
            throw new IllegalArgumentException("outputDir 不能为空");
        }
        this.inputPath = inputPath;
        this.outputDir = outputDir;
        this.segmentType = StrUtil.isBlank(segmentType) ? "fmp4" : segmentType;
        this.hlsTime = Objects.isNull(hlsTime) ? 6 : hlsTime;
        this.audioBitrate = StrUtil.isBlank(audioBitrate) ? "128k" : audioBitrate;
        this.sampleRate = Objects.isNull(sampleRate) ? 48000 : sampleRate;
    }
}