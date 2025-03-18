/**
 * @author kk01001
 * @date 2024-03-13 14:31:00
 * @description 天气数据发送控制器
 */
package io.github.kk01001.examples.design.observer.controller;

import io.github.kk01001.examples.design.observer.WeatherData;
import io.github.kk01001.examples.design.observer.dto.WeatherResponse;
import io.github.kk01001.design.pattern.observer.ObserverFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
@Tag(name = "天气数据接口", description = "用于发送天气数据事件")
public class WeatherController {

    private final ObserverFactory observerFactory;

    @PostMapping("/notify")
    @Operation(summary = "发送天气数据", description = "发送天气数据事件，通知所有观察者")
    public WeatherResponse notifyWeatherData(
            @Parameter(description = "天气数据", required = true) @RequestBody @Validated WeatherData weatherData) {
        try {
            log.info("收到天气数据：{}", weatherData);
            observerFactory.notifyObservers("weather", weatherData);
            return WeatherResponse.success("天气数据已成功发送");
        } catch (Exception e) {
            log.error("发送天气数据失败", e);
            return WeatherResponse.error("发送天气数据失败：" + e.getMessage());
        }
    }
}