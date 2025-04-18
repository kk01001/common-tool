package io.github.kk01001.robot.controller;

import io.github.kk01001.robot.message.*;
import io.github.kk01001.robot.service.RobotService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 机器人消息发送接口
 */
@RestController
@RequestMapping("/robot")
@RequiredArgsConstructor
public class RobotController {

    private final RobotService robotService;

    /**
     * 发送文本消息
     */
    @PostMapping("/{robotId}/text")
    public void sendTextMessage(@PathVariable("robotId") String robotId, @RequestBody TextMessage message) {
        robotService.sendMessage(robotId, message);
    }

    /**
     * 发送Markdown消息
     */
    @PostMapping("/{robotId}/markdown")
    public void sendMarkdownMessage(@PathVariable("robotId") String robotId, @RequestBody MarkdownMessage message) {
        robotService.sendMessage(robotId, message);
    }

    /**
     * 发送链接消息
     */
    @PostMapping("/{robotId}/link")
    public void sendLinkMessage(@PathVariable("robotId") String robotId, @RequestBody LinkMessage message) {
        robotService.sendMessage(robotId, message);
    }

    /**
     * 发送图片消息
     */
    @PostMapping("/{robotId}/image")
    public void sendImageMessage(@PathVariable("robotId") String robotId, @RequestBody ImageMessage message) {
        robotService.sendMessage(robotId, message);
    }

    /**
     * 发送卡片消息
     */
    @PostMapping("/{robotId}/action-card")
    public void sendActionCardMessage(@PathVariable("robotId") String robotId, @RequestBody ActionCardMessage message) {
        robotService.sendMessage(robotId, message);
    }

    /**
     * 发送图文消息
     */
    @PostMapping("/{robotId}/feed-card")
    public void sendFeedCardMessage(@PathVariable("robotId") String robotId, @RequestBody FeedCardMessage message) {
        robotService.sendMessage(robotId, message);
    }

    /**
     * 发送邮件消息
     * 
     * @param robotId 机器人ID，需要配置为邮件类型机器人
     * @param message 邮件消息内容
     */
    @PostMapping("/{robotId}/email")
    public void sendEmailMessage(@PathVariable("robotId") String robotId, @RequestBody EmailMessage message) {
        robotService.sendMessage(robotId, message);
    }

    /**
     * 发送短信消息
     * 
     * @param robotId 机器人ID，需要配置为短信类型机器人
     * @param message 短信消息内容
     */
    @PostMapping("/{robotId}/sms")
    public void sendSmsMessage(@PathVariable("robotId") String robotId, @RequestBody SmsMessage message) {
        robotService.sendMessage(robotId, message);
    }

    /**
     * 发送新闻消息
     * 
     * @param robotId 机器人ID
     * @param message 新闻消息内容
     */
    @PostMapping("/{robotId}/news")
    public void sendNewsMessage(@PathVariable("robotId") String robotId, @RequestBody NewsMessage message) {
        robotService.sendMessage(robotId, message);
    }

    /**
     * 发送文件消息
     * 
     * @param robotId 机器人ID
     * @param message 文件消息内容
     */
    @PostMapping("/{robotId}/file")
    public void sendFileMessage(@PathVariable("robotId") String robotId, @RequestBody FileMessage message) {
        robotService.sendMessage(robotId, message);
    }

    /**
     * 发送语音消息
     * 
     * @param robotId 机器人ID
     * @param message 语音消息内容
     */
    @PostMapping("/{robotId}/voice")
    public void sendVoiceMessage(@PathVariable("robotId") String robotId, @RequestBody VoiceMessage message) {
        robotService.sendMessage(robotId, message);
    }
} 