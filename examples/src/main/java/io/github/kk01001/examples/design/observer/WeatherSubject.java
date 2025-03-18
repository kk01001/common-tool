package io.github.kk01001.examples.design.observer;

import io.github.kk01001.design.pattern.observer.Subject;
import lombok.Getter;

/**
 * @author kk01001
 * @date 2024-03-13 14:31:00
 * @description 天气数据类
 */
@Getter
public class WeatherSubject extends Subject {
    /**
     * 温度
     */
    private final float temperature;

    /**
     * 湿度
     */
    private final float humidity;

    /**
     * 气压
     */
    private final float pressure;

    public WeatherSubject(Object source, float temperature, float humidity, float pressure) {
        super(source);
        this.temperature = temperature;
        this.humidity = humidity;
        this.pressure = pressure;
    }

    @Override
    public String getTopic() {
        return WeatherSubject.class.getSimpleName();
    }
} 