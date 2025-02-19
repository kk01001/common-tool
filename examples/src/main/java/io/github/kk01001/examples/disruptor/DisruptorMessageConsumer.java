package io.github.kk01001.examples.disruptor;

import com.lmax.disruptor.dsl.ProducerType;
import io.github.kk01001.disruptor.annotation.DisruptorListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DisruptorMessageConsumer {

    @DisruptorListener(value = "messageQueue", producerType = ProducerType.SINGLE)
    public void onMessage(Message message) {
        log.info("messageQueue 收到消息: {}", message);
        // 模拟消息处理
    }

    @DisruptorListener(value = "messageQueue2", threads = 2, virtualThread = true)
    public void onMessage2(Message message) {
        log.info("messageQueue2 threadName: {}, group: {}, 收到消息: {}",
                Thread.currentThread().getName(),
                Thread.currentThread().getThreadGroup().getName(),
                message);
        // 模拟消息处理
    }

    @DisruptorListener(value = "messageQueue2", threads = 2, virtualThread = true)
    public void onMessage22(Message message) {
        log.info("messageQueue22 threadName: {}, group: {}, 收到消息: {}",
                Thread.currentThread().getName(),
                Thread.currentThread().getThreadGroup().getName(),
                message);
        // 模拟消息处理
    }
} 