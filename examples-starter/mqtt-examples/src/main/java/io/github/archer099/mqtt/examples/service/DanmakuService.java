package io.github.archer099.mqtt.examples.service;

import io.github.archer099.mqtt.examples.dto.DanmakuSendRequestDTO;
import io.github.archer099.mqtt.examples.vo.DanmakuSendResponseVO;

public interface DanmakuService {

    DanmakuSendResponseVO send(DanmakuSendRequestDTO request);
}