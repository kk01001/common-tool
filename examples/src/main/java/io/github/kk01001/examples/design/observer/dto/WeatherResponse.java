package io.github.kk01001.examples.design.observer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author kk01001
 * @date 2024-03-13 14:31:00
 * @description 天气数据响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "天气数据响应DTO")
public class WeatherResponse {
    
    @Schema(description = "处理结果", example = "success")
    private String result;
    
    @Schema(description = "处理消息", example = "天气数据已发送")
    private String message;
    
    /**
     * 创建成功响应
     *
     * @param message 消息
     * @return 响应对象
     */
    public static WeatherResponse success(String message) {
        return WeatherResponse.builder()
                .result("success")
                .message(message)
                .build();
    }
    
    /**
     * 创建失败响应
     *
     * @param message 消息
     * @return 响应对象
     */
    public static WeatherResponse error(String message) {
        return WeatherResponse.builder()
                .result("error")
                .message(message)
                .build();
    }
} 