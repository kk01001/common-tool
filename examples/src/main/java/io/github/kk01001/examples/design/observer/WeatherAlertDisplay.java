package io.github.kk01001.examples.design.observer;

import io.github.kk01001.design.pattern.observer.IObserver;
import io.github.kk01001.design.pattern.observer.annotation.Observer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author kk01001
 * @date 2024-03-13 14:31:00
 * @description 天气预警显示观察者实现
 */
@Slf4j
@Component
@Observer(topic = "weather")
public class WeatherAlertDisplay implements IObserver<WeatherData> {

    /**
     * 温度阈值（高温预警）
     */
    private static final float HIGH_TEMP_THRESHOLD = 30.0f;

    /**
     * 温度阈值（低温预警）
     */
    private static final float LOW_TEMP_THRESHOLD = 10.0f;

    /**
     * 湿度阈值（高湿预警）
     */
    private static final float HIGH_HUMIDITY_THRESHOLD = 80.0f;

    /**
     * 湿度阈值（低湿预警）
     */
    private static final float LOW_HUMIDITY_THRESHOLD = 30.0f;

    @Override
    public void onUpdate(WeatherData data) {
        checkTemperature(data.getTemperature());
        checkHumidity(data.getHumidity());
    }

    /**
     * 检查温度并发出预警
     *
     * @param temperature 温度
     */
    private void checkTemperature(float temperature) {
        if (temperature > HIGH_TEMP_THRESHOLD) {
            log.warn("⚠️ 高温预警：当前温度 {} 度，超过预警阈值 {} 度", temperature, HIGH_TEMP_THRESHOLD);
        } else if (temperature < LOW_TEMP_THRESHOLD) {
            log.warn("⚠️ 低温预警：当前温度 {} 度，低于预警阈值 {} 度", temperature, LOW_TEMP_THRESHOLD);
        }
    }

    /**
     * 检查湿度并发出预警
     *
     * @param humidity 湿度
     */
    private void checkHumidity(float humidity) {
        if (humidity > HIGH_HUMIDITY_THRESHOLD) {
            log.warn("⚠️ 高湿预警：当前湿度 {}%，超过预警阈值 {}%", humidity, HIGH_HUMIDITY_THRESHOLD);
        } else if (humidity < LOW_HUMIDITY_THRESHOLD) {
            log.warn("⚠️ 低湿预警：当前湿度 {}%，低于预警阈值 {}%", humidity, LOW_HUMIDITY_THRESHOLD);
        }
    }

    @Override
    public int getOrder() {
        // 预警显示优先级最高
        return 0;
    }
}