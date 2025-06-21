package io.github.kk01001.dynamic.mq.core;

import io.github.kk01001.dynamic.mq.model.MqSendResult;

/**
 * @author linshiqiang
 * @date 2025-06-21 21:41:29
 * @description MQ异步发送回调接口，通用的异步发送回调
 */
public interface MqSendCallback {
    
    /**
     * 发送成功回调
     *
     * @param result 发送结果
     */
    void onSuccess(MqSendResult result);
    
    /**
     * 发送失败回调
     *
     * @param exception 异常信息
     */
    void onException(Throwable exception);
}
