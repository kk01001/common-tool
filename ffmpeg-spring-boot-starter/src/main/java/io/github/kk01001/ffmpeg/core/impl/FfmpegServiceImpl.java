package io.github.archer099.ffmpeg.core.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.StrUtil;
import io.github.archer099.exception.BizException;
import io.github.archer099.ffmpeg.FfmpegProperties;
import io.github.archer099.ffmpeg.core.FfmpegService;
import io.github.archer099.ffmpeg.model.AudioHlsRequest;
import io.github.archer099.ffmpeg.model.AudioHlsResult;
import io.github.archer099.ffmpeg.model.FfprobeResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author archer099
 * @date 2025-11-17 16:22:00
 * @description FFmpeg 服务实现
 */
@RequiredArgsConstructor
@Slf4j
public class FfmpegServiceImpl implements FfmpegService {

    private final FfmpegProperties props;

    @Override
    public AudioHlsResult audioToHls(AudioHlsRequest request) {
        ensureEnabled();
        validatePaths(request.getInputPath(), request.getOutputDir());

        String ffmpeg = StrUtil.blankToDefault(props.getFfmpegPath(), "ffmpeg");
        String segType = StrUtil.blankToDefault(request.getSegmentType(), props.getSegmentType());
        int hlsTime = Objects.isNull(request.getHlsTime()) ? props.getHlsTime() : request.getHlsTime();
        String bitrate = StrUtil.blankToDefault(request.getAudioBitrate(), props.getAudioBitrate());
        int sampleRate = Objects.isNull(request.getSampleRate()) ? props.getSampleRate() : request.getSampleRate();

        // 运行前能力检测：确保当前 ffmpeg 构建支持 HLS 与 AAC
        verifyFfmpegSupportsHlsAndAac(ffmpeg, segType);

        File outDir = new File(request.getOutputDir());
        if (!outDir.exists() && !outDir.mkdirs()) {
            throw new BizException("输出目录创建失败: " + outDir.getAbsolutePath());
        }

        String m3u8Path = new File(outDir, "output.m3u8").getAbsolutePath();
        // 为确保播放清单中的片段路径与物理位置一致，分片与 init.mp4 与 m3u8 保持同目录
        // 这样清单中的 "seg_000" 等相对路径可直接命中同目录下的文件

        List<String> cmd = new ArrayList<>();
        cmd.add(ffmpeg);
        cmd.add("-y");
        cmd.add("-i");
        cmd.add(request.getInputPath());
        cmd.add("-vn");
        cmd.add("-ac");
        cmd.add("2");
        cmd.add("-ar");
        cmd.add(String.valueOf(sampleRate));
        cmd.add("-c:a");
        cmd.add("aac");
        cmd.add("-b:a");
        cmd.add(bitrate);
        cmd.add("-f");
        cmd.add("hls");
        cmd.add("-hls_time");
        cmd.add(String.valueOf(hlsTime));
        cmd.add("-hls_playlist_type");
        cmd.add("vod");
        cmd.add("-hls_flags");
        cmd.add("independent_segments");

        if ("fmp4".equalsIgnoreCase(segType)) {
            cmd.add("-hls_segment_type");
            cmd.add("fmp4");
            cmd.add("-hls_fmp4_init_filename");
            cmd.add("init.mp4");
            cmd.add("-hls_segment_filename");
            cmd.add(new File(outDir, "seg_%03d.m4s").getAbsolutePath());
        } else {
            cmd.add("-hls_segment_filename");
            cmd.add(new File(outDir, "seg_%03d.ts").getAbsolutePath());
        }

        cmd.add(m3u8Path);

        // 使用 Hutool 输出完整命令行，便于排查与复现
        String ffmpegCmdLine = CollUtil.join(cmd, " ");
        log.info("[FFmpeg] 执行命令: {}", ffmpegCmdLine);
        /*
         完整命令及参数说明：
         -y                      覆盖输出文件
         -i <input>              输入媒体文件
         -vn                     禁用视频流，仅处理音频
         -ac 2                   双声道音频
         -ar <sampleRate>        音频采样率（如 48000）
         -c:a aac                音频编码器为 AAC
         -b:a <bitrate>          音频码率（如 128k）
         -f hls                  输出为 HLS 格式
         -hls_time <seconds>     HLS 分片时长（秒）
         -hls_playlist_type vod  生成点播清单
         -hls_flags independent_segments 独立分片，便于按需加载
         -hls_segment_type fmp4  当分片类型为 fMP4 时使用
         -hls_fmp4_init_filename 指定初始化片段文件名（如 init.mp4）
         -hls_segment_filename   分片文件命名模式（seg_%03d.ts 或 seg_%03d.m4s）
        */

        long start = System.currentTimeMillis();
        run(cmd, Duration.ofSeconds(props.getTimeoutSeconds()));
        long duration = System.currentTimeMillis() - start;

        AudioHlsResult result = new AudioHlsResult();
        result.setM3u8Path(m3u8Path);
        result.setSegmentDir(outDir.getAbsolutePath());
        result.setSegmentCount(null); // 留空：可进一步用 ffprobe 或直接统计文件数
        result.setDurationMs(duration);
        return result;
    }

    @Override
    public FfprobeResult ffprobe(String inputPath) {
        ensureEnabled();
        validateInput(inputPath);

        String ffprobe = StrUtil.blankToDefault(props.getFfprobePath(), "ffprobe");
        List<String> cmd = new ArrayList<>();
        cmd.add(ffprobe);
        cmd.add("-v");
        cmd.add("error");
        cmd.add("-show_format");
        cmd.add("-show_streams");
        cmd.add("-print_format");
        cmd.add("json");
        cmd.add(inputPath);

        // 使用 Hutool 输出完整命令行，便于排查与复现
        String ffprobeCmdLine = CollUtil.join(cmd, " ");
        log.info("[FFprobe] 执行命令: {}", ffprobeCmdLine);
        /*
         完整命令及参数说明：
         -v error           仅输出错误，减少冗余日志
         -show_format       显示容器格式信息
         -show_streams      显示各媒体流（音视频）信息
         -print_format json 以 JSON 形式输出结果，便于解析
        */

        String output = runAndCapture(cmd, Duration.ofSeconds(props.getTimeoutSeconds()));
        FfprobeResult result = new FfprobeResult();
        result.setRawJson(output);
        return result;
    }

    private void ensureEnabled() {
        if (!Boolean.TRUE.equals(props.getEnabled())) {
            throw new BizException("请先开启 ffmpeg.enabled");
        }
    }

    private void validatePaths(String inputPath, String outputDir) {
        validateInput(inputPath);
        if (StrUtil.isBlank(outputDir)) {
            throw new BizException("outputDir 不能为空");
        }
    }

    private void validateInput(String inputPath) {
        if (StrUtil.isBlank(inputPath)) {
            throw new BizException("inputPath 不能为空");
        }
        File in = new File(inputPath);
        if (!in.exists() || !in.isFile()) {
            throw new BizException("输入文件不存在: " + inputPath);
        }
    }

    private void run(List<String> cmd, Duration timeout) {
        try {
            String output = RuntimeUtil.execForStr(cmd.toArray(new String[0]));
            log.info("[FFmpeg] 命令输出: {}", StrUtil.blankToDefault(output, "<empty>"));
        } catch (IORuntimeException e) {
            throw new BizException("FFmpeg 执行异常: " + e.getMessage());
        }
    }

    private String runAndCapture(List<String> cmd, Duration timeout) {
        try {
            String output = RuntimeUtil.execForStr(cmd.toArray(new String[0]));
            log.info("[FFprobe] 命令输出: {}", StrUtil.blankToDefault(output, "<empty>"));
            return output;
        } catch (IORuntimeException e) {
            throw new BizException("ffprobe 执行异常: " + e.getMessage());
        }
    }

    /**
     * 检查当前 ffmpeg 是否支持 HLS muxer 与 AAC 编码器。
     * 当 segType = fmp4 时仍依赖 HLS muxer，但分片类型为 fMP4。
     */
    private void verifyFfmpegSupportsHlsAndAac(String ffmpegPath, String segType) {
        // 检查 muxers 列表是否包含 hls
        List<String> muxersCmd = new ArrayList<>();
        muxersCmd.add(ffmpegPath);
        muxersCmd.add("-muxers");
        String muxersOut = runAndCaptureWithTag(muxersCmd, Duration.ofSeconds(10), "FFmpeg");
        if (!StrUtil.containsIgnoreCase(muxersOut, "hls")) {
            throw new BizException("当前 FFmpeg 构建不包含 HLS muxer，请更换完整版本的 ffmpeg（需支持 hls）。");
        }

        // 检查编码器是否包含 aac（音频编码器）
        List<String> encodersCmd = new ArrayList<>();
        encodersCmd.add(ffmpegPath);
        encodersCmd.add("-encoders");
        String encodersOut = runAndCaptureWithTag(encodersCmd, Duration.ofSeconds(10), "FFmpeg");
        if (!StrUtil.containsIgnoreCase(encodersOut, "aac")) {
            throw new BizException("当前 FFmpeg 构建不包含 AAC 编码器，请更换完整版本的 ffmpeg（需支持 aac）。");
        }

        // 对 fmp4 分片类型的提示（不强制检查）
        if ("fmp4".equalsIgnoreCase(segType)) {
            log.info("[FFmpeg] 使用 fMP4 分片类型，需 ffmpeg 支持相关 MP4 封装组件。");
        }
    }

    /**
     * 通用命令执行并捕获输出（带 tag 标签）。
     */
    private String runAndCaptureWithTag(List<String> cmd, Duration timeout, String tag) {
        try {
            String output = RuntimeUtil.execForStr(cmd.toArray(new String[0]));
            log.info("[{}] 命令输出: {}", tag, StrUtil.blankToDefault(output, "<empty>"));
            return output;
        } catch (IORuntimeException e) {
            throw new BizException(tag + " 执行异常: " + e.getMessage());
        }
    }

}