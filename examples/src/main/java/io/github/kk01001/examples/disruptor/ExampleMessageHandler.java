package io.github.kk01001.examples.disruptor;

import io.github.kk01001.disruptor.event.DisruptorEvent;
import io.github.kk01001.disruptor.handler.MessageHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExampleMessageHandler implements MessageHandler<Message> {

    @Override
    public void handle(DisruptorEvent<Message> event) {
        log.info("ExampleMessageHandler threadName: {}, group: {}, 收到消息: {}",
                Thread.currentThread().getName(),
                Thread.currentThread().getThreadGroup().getName(),
                event.getData());
        // 在这里实现具体的消息处理逻辑
        // try {
        //     TimeUnit.SECONDS.sleep(1);
        // } catch (InterruptedException e) {
        //     throw new RuntimeException(e);
        // }
    }
}