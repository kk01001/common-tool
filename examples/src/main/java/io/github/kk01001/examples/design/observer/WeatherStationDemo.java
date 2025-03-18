/**
 * @author kk01001
 * @date 2024-03-13 14:31:00
 * @description 天气站示例测试
 */
package io.github.kk01001.examples.design.observer;

import io.github.kk01001.design.pattern.observer.ObserverFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeatherStationDemo implements CommandLineRunner {

    private final ObserverFactory observerFactory;

    @Override
    public void run(String... args) {
        log.info("============= 正常天气 =============");
        // 发布正常天气数据
        WeatherData weatherData = WeatherData.builder()
                .temperature(25.5f)
                .humidity(65)
                .pressure(1013.2f)
                .build();
        observerFactory.notifyObservers("weather", weatherData);

        log.info("============= 高温天气 =============");
        // 发布高温天气数据
        weatherData = WeatherData.builder()
                .temperature(32.8f)
                .humidity(70)
                .pressure(1012.8f)
                .build();
        observerFactory.notifyObservers("weather", weatherData);

        log.info("============= 低温天气 =============");
        // 发布低温天气数据
        weatherData = WeatherData.builder()
                .temperature(8.4f)
                .humidity(45)
                .pressure(1013.0f)
                .build();
        observerFactory.notifyObservers("weather", weatherData);

        log.info("============= 高湿度天气 =============");
        // 发布高湿度天气数据
        weatherData = WeatherData.builder()
                .temperature(26.4f)
                .humidity(85)
                .pressure(1013.0f)
                .build();
        observerFactory.notifyObservers("weather", weatherData);

        log.info("============= 低湿度天气 =============");
        // 发布低湿度天气数据
        weatherData = WeatherData.builder()
                .temperature(26.4f)
                .humidity(25)
                .pressure(1013.0f)
                .build();
        observerFactory.notifyObservers("weather", weatherData);
    }
} 