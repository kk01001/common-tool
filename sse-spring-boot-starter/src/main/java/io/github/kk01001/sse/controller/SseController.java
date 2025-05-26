package io.github.kk01001.sse.controller;

import cn.hutool.core.util.StrUtil;
import io.github.kk01001.sse.manager.SseConnectionManager;
import io.github.kk01001.sse.model.SseMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description SSE控制器
 */
@Slf4j
@ResponseBody
@RequestMapping("/sse")
@RequiredArgsConstructor
public class SseController {

    /**
     * SSE连接管理器
     */
    private final SseConnectionManager connectionManager;

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
        log.debug("创建SSE连接，clientId: {}, userId: {}, topic: {}", clientId, userId, topic);
        return connectionManager.createConnection(clientId, userId, topic);
    }

    /**
     * 关闭SSE连接
     *
     * @param clientId 客户端ID
     * @return 结果
     */
    @GetMapping("/disconnect")
    public String disconnect(@RequestParam("clientId") String clientId) {
        if (StrUtil.isBlank(clientId)) {
            return "clientId不能为空";
        }
        connectionManager.closeConnection(clientId);
        return "连接已关闭";
    }

    /**
     * 发送消息
     *
     * @param message 消息
     * @return 结果
     */
    @PostMapping("/send")
    public String send(@RequestBody SseMessage message) {
        if (message == null) {
            return "消息不能为空";
        }
        connectionManager.sendMessage(message);
        return "消息已发送";
    }

    /**
     * 向指定客户端发送消息
     *
     * @param clientId 客户端ID
     * @param message  消息
     * @return 结果
     */
    @PostMapping("/send/client/{clientId}")
    public String sendToClient(@PathVariable("clientId") String clientId, @RequestBody SseMessage message) {
        if (StrUtil.isBlank(clientId)) {
            return "clientId不能为空";
        }
        if (message == null) {
            return "消息不能为空";
        }
        message.setClientId(clientId);
        connectionManager.sendMessage(message);
        return "消息已发送";
    }

    /**
     * 向指定用户发送消息
     *
     * @param userId  用户ID
     * @param message 消息
     * @return 结果
     */
    @PostMapping("/send/user/{userId}")
    public String sendToUser(@PathVariable("userId") String userId, @RequestBody SseMessage message) {
        if (StrUtil.isBlank(userId)) {
            return "userId不能为空";
        }
        if (message == null) {
            return "消息不能为空";
        }
        message.setUserId(userId);
        connectionManager.sendMessage(message);
        return "消息已发送";
    }

    /**
     * 向指定主题发送消息
     *
     * @param topic   主题
     * @param message 消息
     * @return 结果
     */
    @PostMapping("/send/topic/{topic}")
    public String sendToTopic(@PathVariable("topic") String topic, @RequestBody SseMessage message) {
        if (StrUtil.isBlank(topic)) {
            return "topic不能为空";
        }
        if (message == null) {
            return "消息不能为空";
        }
        message.setTopic(topic);
        connectionManager.sendMessage(message);
        return "消息已发送";
    }

    /**
     * 广播消息
     *
     * @param message 消息
     * @return 结果
     */
    @PostMapping("/broadcast")
    public String broadcast(@RequestBody SseMessage message) {
        if (message == null) {
            return "消息不能为空";
        }
        message.setBroadcast(true);
        connectionManager.sendMessage(message);
        return "消息已广播";
    }

    /**
     * 获取连接数量
     *
     * @return 连接数量
     */
    @GetMapping("/count")
    public int count() {
        return connectionManager.getConnectionCount();
    }
}
