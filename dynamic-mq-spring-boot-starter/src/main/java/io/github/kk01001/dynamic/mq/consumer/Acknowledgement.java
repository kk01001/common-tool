package io.github.kk01001.dynamic.mq.consumer;

/**
 * @author linshiqiang
 * @date 2025-06-21 20:48:56
 * @description 消息确认接口
 */
@FunctionalInterface
public interface Acknowledgement {
    
    /**
     * 确认消息处理成功
     */
    void acknowledge();
}
