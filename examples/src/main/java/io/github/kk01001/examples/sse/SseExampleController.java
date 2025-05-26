package io.github.kk01001.examples.sse;

import io.github.kk01001.sse.model.SseMessage;
import io.github.kk01001.sse.service.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description SSE示例控制器
 */
@CrossOrigin
@Slf4j
@RestController
@RequestMapping("/api/sse")
@RequiredArgsConstructor
public class SseExampleController {

    private final SseService sseService;

    /**
     * 创建SSE连接
     *
     * @param clientId 客户端ID，可选
     * @param userId   用户ID，可选
     * @param topic    主题，可选
     * @return SseEmitter
     */
    @GetMapping("/connect")
    public SseEmitter connect(
            @RequestParam(value = "clientId", required = false) String clientId,
            @RequestParam(value = "userId", required = false) String userId,
            @RequestParam(value = "topic", required = false) String topic) {
        log.info("创建SSE连接，clientId: {}, userId: {}, topic: {}", clientId, userId, topic);
        return sseService.createConnection(clientId, userId, topic);
    }

    /**
     * 发送消息给指定用户
     *
     * @param userId  用户ID
     * @param message 消息内容
     * @return 结果
     */
    @PostMapping("/send/user/{userId}")
    public String sendToUser(@PathVariable("userId") String userId, @RequestParam("message") String message) {
        log.info("发送消息给用户: {}, 消息内容: {}", userId, message);
        sseService.sendMessageToUser(userId, message, "user-message");
        return "消息已发送给用户: " + userId;
    }

    /**
     * 发送消息给指定主题
     *
     * @param topic   主题
     * @param message 消息内容
     * @return 结果
     */
    @PostMapping("/send/topic/{topic}")
    public String sendToTopic(@PathVariable("topic") String topic, @RequestParam("message") String message) {
        log.info("发送消息给主题: {}, 消息内容: {}", topic, message);
        sseService.sendMessageToTopic(topic, message, "topic-message");
        return "消息已发送给主题: " + topic;
    }

    /**
     * 广播消息
     *
     * @param message 消息内容
     * @return 结果
     */
    @PostMapping("/broadcast")
    public String broadcast(@RequestParam("message") String message) {
        log.info("广播消息: {}", message);
        sseService.broadcastMessage(message, "broadcast");
        return "消息已广播";
    }

    /**
     * 发送自定义消息
     *
     * @param message 消息对象
     * @return 结果
     */
    @PostMapping("/send/custom")
    public String sendCustomMessage(@RequestBody SseMessage message) {
        if (message.getId() == null) {
            message.setId(UUID.randomUUID().toString());
        }
        log.info("发送自定义消息: {}", message);
        sseService.sendMessage(message);
        return "自定义消息已发送";
    }

    /**
     * 获取当前连接数
     *
     * @return 连接数
     */
    @GetMapping("/count")
    public int getConnectionCount() {
        return sseService.getConnectionCount();
    }
}
