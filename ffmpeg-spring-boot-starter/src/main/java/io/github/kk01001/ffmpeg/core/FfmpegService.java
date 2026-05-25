package io.github.archer099.ffmpeg.core;

import io.github.archer099.ffmpeg.model.AudioHlsRequest;
import io.github.archer099.ffmpeg.model.AudioHlsResult;
import io.github.archer099.ffmpeg.model.FfprobeResult;

/**
 * @author archer099
 * @date 2025-11-17 16:22:00
 * @description FFmpeg 服务接口
 */
public interface FfmpegService {

    /**
     * 将音频转为 HLS（m3u8 + 分片）
     */
    AudioHlsResult audioToHls(AudioHlsRequest request);

    /**
     * 使用 ffprobe 获取媒体信息
     */
    FfprobeResult ffprobe(String inputPath);
}