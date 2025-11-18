package io.github.kk01001.mqtt.examples.service;

import io.github.kk01001.mqtt.examples.dto.MqttDelayedSendRequestDTO;

public interface MqttDelayService {

    void sendDelayed(MqttDelayedSendRequestDTO request);
}