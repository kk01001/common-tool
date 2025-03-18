package io.github.kk01001.examples.design.observer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author kk01001
 * @date 2024-03-13 14:31:00
 * @description 天气数据类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherData {
    /**
     * 温度
     */
    private float temperature;
    
    /**
     * 湿度
     */
    private float humidity;
    
    /**
     * 气压
     */
    private float pressure;
} 