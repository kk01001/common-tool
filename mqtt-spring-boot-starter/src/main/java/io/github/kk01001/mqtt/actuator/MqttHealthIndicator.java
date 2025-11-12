package io.github.kk01001.mqtt.actuator;

import io.github.kk01001.mqtt.core.MqttClientManager;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * MQTT 健康检查指示器
 *
 * @author kk01001
 */
@Component
@RequiredArgsConstructor
public class MqttHealthIndicator implements HealthIndicator {

    private final MqttClientManager mqttClientManager;

    @Override
    public Health health() {
        try {
            if (mqttClientManager.isConnected()) {
                return Health.up()
                        .withDetail("status", "connected")
                        .withDetail("clientId", mqttClientManager.getMqttClient().getClientId())
                        .withDetail("serverURI", mqttClientManager.getMqttClient().getServerURI())
                        .build();
            } else {
                return Health.down()
                        .withDetail("status", "disconnected")
                        .withDetail("reason", "MQTT client is not connected")
                        .build();
            }
        } catch (Exception e) {
            return Health.down()
                    .withDetail("status", "error")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
