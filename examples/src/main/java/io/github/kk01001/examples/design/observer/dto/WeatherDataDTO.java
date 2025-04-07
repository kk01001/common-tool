package io.github.kk01001.examples.design.observer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author kk01001
 * @date 2024-03-13 14:31:00
 * @description 天气数据传输对象
 */
@Data
@Schema(description = "天气数据传输对象")
public class WeatherDataDTO {

    @Schema(description = "温度（摄氏度）", example = "25.5")
    private float temperature;
    
    @Schema(description = "湿度（百分比）", example = "65")
    private float humidity;
    
    @Schema(description = "气压（百帕）", example = "1013.2")
    private float pressure;
} 