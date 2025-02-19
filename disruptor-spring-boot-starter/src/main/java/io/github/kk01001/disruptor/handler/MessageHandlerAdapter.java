package io.github.kk01001.disruptor.handler;

import com.lmax.disruptor.EventHandler;
import io.github.kk01001.disruptor.event.DisruptorEvent;

/**
 * @author kk01001
 * @date 2025-02-19 10:15:47
 * @description 消息处理器适配器
 */
public class MessageHandlerAdapter<T> implements EventHandler<DisruptorEvent<T>> {

    private final MessageHandler<T> messageHandler;

    public MessageHandlerAdapter(MessageHandler<T> messageHandler) {
        this.messageHandler = messageHandler;
    }

    @Override
    public void onEvent(DisruptorEvent<T> event, long sequence, boolean endOfBatch) {
        messageHandler.handle(event);
    }
}