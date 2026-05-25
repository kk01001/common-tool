package io.github.archer099.mqtt.examples.service.impl;

import cn.hutool.core.util.StrUtil;
import io.github.archer099.exception.BizException;
import io.github.archer099.mqtt.core.MqttTemplate;
import io.github.archer099.mqtt.examples.dto.MqttDelayedSendRequestDTO;
import io.github.archer099.mqtt.examples.service.MqttDelayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class MqttDelayServiceImpl implements MqttDelayService {

    private final MqttTemplate mqttTemplate;

    @Override
    public void sendDelayed(MqttDelayedSendRequestDTO request) {
        if (Objects.isNull(request) || StrUtil.isBlank(request.getTopic())) {
            throw new BizException("topic不能为空");
        }
        if (Objects.isNull(request.getDelaySeconds()) || request.getDelaySeconds() <= 0) {
            throw new BizException("延迟秒数不合法");
        }
        if (StrUtil.isBlank(request.getPayload())) {
            throw new BizException("消息内容不能为空");
        }
        try {
            mqttTemplate.sendDelayed(request.getTopic(), request.getPayload(), request.getDelaySeconds());
        } catch (MqttException e) {
            log.error("延迟发布失败, topic={}, delaySeconds={}", request.getTopic(), request.getDelaySeconds(), e);
            throw new BizException("延迟发布失败");
        }
    }
}