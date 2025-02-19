package io.github.kk01001.examples.disruptor;

import io.github.kk01001.disruptor.handler.MessageHandler;
import io.github.kk01001.disruptor.event.DisruptorEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExampleMessageHandler implements MessageHandler<Message> {

    @Override
    public void handle(DisruptorEvent<Message> event) {
        log.info("Processing message: {}", event.getData());
        // 在这里实现具体的消息处理逻辑
    }
}