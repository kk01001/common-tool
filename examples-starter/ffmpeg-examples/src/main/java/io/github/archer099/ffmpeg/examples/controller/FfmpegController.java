package io.github.archer099.ffmpeg.examples.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import io.github.archer099.common.model.ApiResponse;
import io.github.archer099.ffmpeg.FfmpegProperties;
import io.github.archer099.ffmpeg.core.FfmpegService;
import io.github.archer099.ffmpeg.examples.dto.AudioHlsConvertDTO;
import io.github.archer099.ffmpeg.examples.dto.FfprobeRequestDTO;
import io.github.archer099.ffmpeg.examples.vo.AudioHlsConvertResultVO;
import io.github.archer099.ffmpeg.examples.vo.FfprobeResultVO;
import io.github.archer099.ffmpeg.model.AudioHlsRequest;
import io.github.archer099.ffmpeg.model.AudioHlsResult;
import io.github.archer099.ffmpeg.model.FfprobeResult;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * @author archer099
 * @date 2025-11-17 16:35:00
 * @description FFmpeg 示例接口：音频转 HLS 与 ffprobe
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ffmpeg")
@Slf4j
public class FfmpegController {

    private final FfmpegService ffmpegService;
    private final FfmpegProperties ffmpegProperties;

    /**
     * 音频转 HLS（m3u8 + 分片）
     */
    @PostMapping("/audio/hls")
    @Schema(description = "音频转 HLS（m3u8 + 分片）")
    public ApiResponse<AudioHlsConvertResultVO> audioToHls(@Valid @RequestBody AudioHlsConvertDTO dto) {
        log.info("[FFmpeg] audioToHls req: input={}, outputDir={} segType={} hlsTime={}",
                dto.getInputPath(), dto.getOutputDir(), dto.getSegmentType(), dto.getHlsTime());

        AudioHlsRequest req = new AudioHlsRequest(
                dto.getInputPath(),
                dto.getOutputDir(),
                StrUtil.nullToEmpty(dto.getSegmentType()),
                dto.getHlsTime(),
                StrUtil.nullToEmpty(dto.getAudioBitrate()),
                dto.getSampleRate()
        );
        AudioHlsResult result = ffmpegService.audioToHls(req);

        AudioHlsConvertResultVO vo = new AudioHlsConvertResultVO();
        vo.setM3u8Path(result.getM3u8Path());
        vo.setSegmentDir(result.getSegmentDir());
        vo.setSegmentCount(result.getSegmentCount());
        vo.setDurationMs(result.getDurationMs());
        log.info("[FFmpeg] audioToHls done: m3u8={}, segDir={} cost={}ms",
                vo.getM3u8Path(), vo.getSegmentDir(), vo.getDurationMs());
        return ApiResponse.ok(vo);
    }

    /**
     * 上传 MP3 并转换为 HLS，返回可播放的 m3u8 路径
     */
    @PostMapping("/audio/hls/upload")
    @Schema(description = "上传 MP3 并转换为 HLS，返回可播放的 m3u8 路径")
    public ApiResponse<AudioHlsConvertResultVO> uploadAndConvert(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "segmentType", required = false) String segmentType,
            @RequestParam(value = "hlsTime", required = false) Integer hlsTime,
            @RequestParam(value = "audioBitrate", required = false) String audioBitrate,
            @RequestParam(value = "sampleRate", required = false) Integer sampleRate
    ) throws IOException {
        if (file == null || file.isEmpty()) {
            return ApiResponse.fail(400);
        }

        // 工作目录：用于承载上传与转换输出，同时作为静态资源目录暴露
        String workDir = StrUtil.blankToDefault(ffmpegProperties.getWorkDir(),
                new File(System.getProperty("user.dir"), "hls").getAbsolutePath());
        File base = new File(workDir);
        if (!base.exists()) {
            FileUtil.mkdir(base);
        }

        String batch = IdUtil.fastSimpleUUID();
        File uploadDir = new File(base, "uploads/" + batch);
        File outputDir = new File(base, "outputs/" + batch);
        FileUtil.mkdir(uploadDir);
        FileUtil.mkdir(outputDir);

        String originalName = StrUtil.blankToDefault(file.getOriginalFilename(), "input.mp3");
        File saved = new File(uploadDir, originalName);
        file.transferTo(saved);
        log.info("[FFmpeg] 保存上传文件: {}", saved.getAbsolutePath());

        // 构建转换请求
        AudioHlsRequest req = new AudioHlsRequest(
                saved.getAbsolutePath(),
                outputDir.getAbsolutePath(),
                StrUtil.nullToEmpty(segmentType),
                hlsTime,
                StrUtil.nullToEmpty(audioBitrate),
                sampleRate
        );
        try {
            AudioHlsResult result = ffmpegService.audioToHls(req);

            // 计算静态资源可访问路径（相对于 workDir）供前端播放
            String absWork = base.getAbsolutePath();
            String publicM3u8 = StrUtil.removePrefix(result.getM3u8Path(), absWork);
            publicM3u8 = publicM3u8.replace('\\', '/');
            if (!publicM3u8.startsWith("/")) {
                publicM3u8 = "/" + publicM3u8;
            }

            AudioHlsConvertResultVO vo = new AudioHlsConvertResultVO();
            vo.setM3u8Path(publicM3u8);
            vo.setSegmentDir(result.getSegmentDir());
            vo.setSegmentCount(result.getSegmentCount());
            vo.setDurationMs(result.getDurationMs());
            // 额外返回服务端保存的输入路径，便于后续 ffprobe
            vo.setInputPath(saved.getAbsolutePath());
            log.info("[FFmpeg] upload+convert done: m3u8(public)={}, savedInput={}",
                    vo.getM3u8Path(), vo.getInputPath());
            return ApiResponse.ok(vo);
        } catch (Exception ex) {
            // 转换失败，清理本次创建的 uploads 与 outputs 目录
            log.error("[FFmpeg] upload+convert failed, will cleanup. batch={}, error={}", batch, ex.getMessage(), ex);
            try {
                if (outputDir.exists()) {
                    FileUtil.del(outputDir);
                    log.info("[FFmpeg] cleaned output dir: {}", outputDir.getAbsolutePath());
                }
            } catch (Exception cleanEx) {
                log.warn("[FFmpeg] failed to delete output dir: {}", outputDir.getAbsolutePath(), cleanEx);
            }
            try {
                if (uploadDir.exists()) {
                    FileUtil.del(uploadDir);
                    log.info("[FFmpeg] cleaned upload dir: {}", uploadDir.getAbsolutePath());
                }
            } catch (Exception cleanEx) {
                log.warn("[FFmpeg] failed to delete upload dir: {}", uploadDir.getAbsolutePath(), cleanEx);
            }
            throw ex;
        }
    }

    /**
     * 获取媒体文件信息（ffprobe）
     */
    @PostMapping("/ffprobe")
    @Schema(description = "获取媒体文件信息（ffprobe）")
    public ApiResponse<FfprobeResultVO> ffprobe(@Valid @RequestBody FfprobeRequestDTO dto) {
        log.info("[FFmpeg] ffprobe req: input={}", dto.getInputPath());
        FfprobeResult res = ffmpegService.ffprobe(dto.getInputPath());
        FfprobeResultVO vo = new FfprobeResultVO();
        vo.setRawJson(res.getRawJson());
        log.info("[FFmpeg] ffprobe done: size={} bytes", StrUtil.length(vo.getRawJson()));
        return ApiResponse.ok(vo);
    }
}