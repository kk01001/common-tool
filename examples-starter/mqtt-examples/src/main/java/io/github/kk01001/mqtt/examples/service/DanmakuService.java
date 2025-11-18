package io.github.kk01001.mqtt.examples.service;

import io.github.kk01001.mqtt.examples.dto.DanmakuSendRequestDTO;
import io.github.kk01001.mqtt.examples.vo.DanmakuSendResponseVO;

public interface DanmakuService {

    DanmakuSendResponseVO send(DanmakuSendRequestDTO request);
}