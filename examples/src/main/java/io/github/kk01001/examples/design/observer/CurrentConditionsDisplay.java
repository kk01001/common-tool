/**
 * @author kk01001
 * @date 2024-03-13 14:31:00
 * @description 当前天气显示观察者实现
 */
package io.github.kk01001.examples.design.observer;

import io.github.kk01001.design.pattern.observer.IObserver;
import io.github.kk01001.design.pattern.observer.annotation.Observer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Observer
public class CurrentConditionsDisplay implements IObserver<WeatherSubject> {
    private float temperature;
    private float humidity;
    private float pressure;

    @Override
    public void onUpdate(WeatherSubject data) {
        this.temperature = data.getTemperature();
        this.humidity = data.getHumidity();
        this.pressure = data.getPressure();
        display();
    }

    /**
     * 显示当前天气状况
     */
    public void display() {
        log.info("当前天气状况：");
        log.info("温度：{} 度", temperature);
        log.info("湿度：{}%", humidity);
        log.info("气压：{} hPa", pressure);
    }

    @Override
    public int getOrder() {
        return 1;
    }
}