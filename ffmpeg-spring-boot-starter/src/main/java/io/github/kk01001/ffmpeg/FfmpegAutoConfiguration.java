package io.github.kk01001.ffmpeg;

import io.github.kk01001.ffmpeg.core.FfmpegService;
import io.github.kk01001.ffmpeg.core.impl.FfmpegServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author kk01001
 * @date 2025-11-17 16:22:00
 * @description FFmpeg 自动配置
 */
@Configuration
@RequiredArgsConstructor
public class FfmpegAutoConfiguration {

    private final FfmpegProperties properties;

    @Bean
    public FfmpegService ffmpegService() {
        return new FfmpegServiceImpl(properties);
    }
}