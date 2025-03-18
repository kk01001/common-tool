package io.github.kk01001.examples.design.observer;

import io.github.kk01001.design.pattern.observer.IObserver;
import io.github.kk01001.design.pattern.observer.annotation.Observer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author kk01001
 * @date 2024-03-13 14:31:00
 * @description 天气统计信息显示观察者实现
 */
@Slf4j
@Component
@Observer(topic = "weather")
public class StatisticsDisplay implements IObserver<WeatherData> {
    private float maxTemp = Float.MIN_VALUE;
    private float minTemp = Float.MAX_VALUE;
    private float tempSum = 0.0f;
    private int numReadings = 0;

    @Override
    public void onUpdate(WeatherData data) {
        float temperature = data.getTemperature();
        tempSum += temperature;
        numReadings++;

        if (temperature > maxTemp) {
            maxTemp = temperature;
        }

        if (temperature < minTemp) {
            minTemp = temperature;
        }

        display();
    }

    /**
     * 显示统计信息
     */
    public void display() {
        log.info("气温统计信息：");
        log.info("平均温度：{} 度", tempSum / numReadings);
        log.info("最高温度：{} 度", maxTemp);
        log.info("最低温度：{} 度", minTemp);
    }

    @Override
    public int getOrder() {
        return 2;
    }
}