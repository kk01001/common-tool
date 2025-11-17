package io.github.kk01001.mqtt.examples.service;

import io.github.kk01001.mqtt.examples.dto.EmqxAuthRequestDTO;
import io.github.kk01001.mqtt.examples.vo.EmqxAuthResponseVO;

/**
 * @author kk01001
 * @date 2025-11-17 15:45:00
 * @description EMQX HTTP 鉴权服务接口
 */
public interface EmqxAuthService {

    EmqxAuthResponseVO authenticate(EmqxAuthRequestDTO request);
}