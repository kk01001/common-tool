package io.github.kk01001.examples.disruptor;

import cn.hutool.core.util.IdUtil;
import com.lmax.disruptor.dsl.ProducerType;
import io.github.kk01001.disruptor.annotation.WaitStrategyType;
import io.github.kk01001.disruptor.handler.MessageHandler;
import io.github.kk01001.disruptor.template.DisruptorTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ThreadFactory;

@Slf4j
@RestController
@RequestMapping("/message")
@RequiredArgsConstructor
public class DisruptorMessageController {

    private final DisruptorTemplate disruptorTemplate;

    /**
     * 发送消息
     */
    @PostMapping("/send")
    public Message send(@RequestParam(defaultValue = "messageQueue") String queueName,
                        String content) {
        Message message = new Message();
        message.setId(IdUtil.fastSimpleUUID());
        message.setContent(content);
        message.setTimestamp(System.currentTimeMillis());

        disruptorTemplate.send(queueName, message);
        return message;
    }

    /**
     * 批量发送测试消息
     */
    @PostMapping("/send/batch")
    public String sendBatch(@RequestParam(defaultValue = "messageQueue") String queueName,
                            @RequestParam(defaultValue = "10") int count) {
        for (int i = 0; i < count; i++) {
            Message message = new Message();
            message.setId(IdUtil.fastSimpleUUID());
            message.setContent("测试消息-" + i);
            message.setTimestamp(System.currentTimeMillis());

            disruptorTemplate.send(queueName, message);
        }
        return "已发送" + count + "条消息";
    }

    /**
     * 手动创建队列和注册消费者
     */
    @PostMapping("/register-handler")
    public String registerHandler(
            @RequestParam String queueName,
            @RequestParam(defaultValue = "1024") int bufferSize,
            @RequestParam(defaultValue = "SINGLE") ProducerType producerType,
            @RequestParam(defaultValue = "YIELDING") WaitStrategyType waitStrategyType) {

        // 创建虚拟线程工厂
        ThreadFactory threadFactory = Thread.ofVirtual().name("Handler-Thread").factory();

        // 创建消息处理器实例
        MessageHandler<Message> messageHandler = new ExampleMessageHandler();

        // 创建并注册队列
        disruptorTemplate.createQueue(
                queueName,
                bufferSize,
                producerType,
                waitStrategyType.create(),
                threadFactory,
                messageHandler
        );

        return "Handler registered for queue: " + queueName;
    }
}