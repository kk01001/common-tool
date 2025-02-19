package io.github.kk01001.examples.disruptor;

import cn.hutool.core.util.IdUtil;
import io.github.kk01001.disruptor.template.DisruptorTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}