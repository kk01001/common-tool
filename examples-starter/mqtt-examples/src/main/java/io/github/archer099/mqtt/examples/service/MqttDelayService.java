package io.github.archer099.mqtt.examples.service;

import io.github.archer099.mqtt.examples.dto.MqttDelayedSendRequestDTO;

public interface MqttDelayService {

    void sendDelayed(MqttDelayedSendRequestDTO request);
}