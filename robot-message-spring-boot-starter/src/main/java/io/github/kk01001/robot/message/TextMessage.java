package io.github.kk01001.robot.message;

import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文本消息
 * 用于发送简单的文本内容
 */
@Data
public class TextMessage implements RobotMessage {
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 需要@的手机号列表
     */
    private List<String> atMobiles;
    
    /**
     * 需要@的用户ID列表
     */
    private List<String> atUserIds;
    
    /**
     * 是否@所有人
     */
    private Boolean atAll;
    
    @Override
    public String getType() {
        return "text";
    }
    
    @Override
    public Map<String, Object> toMessageMap() {
        Map<String, Object> message = new HashMap<>();
        message.put("msgtype", "text");
        
        Map<String, Object> text = new HashMap<>();
        text.put("content", content);
        message.put("text", text);
        
        if(atMobiles != null || atUserIds != null || atAll != null) {
            Map<String, Object> at = new HashMap<>();
            at.put("atMobiles", atMobiles);
            at.put("atUserIds", atUserIds);
            at.put("isAtAll", atAll);
            message.put("at", at);
        }

        return message;
    }

    @Override
    public Map<String, Object> toMessageMap(String robotType) {
        if ("dingtalk".equals(robotType)) {
            return toMessageMap();
        }
        Map<String, Object> message = new HashMap<>();
        message.put("msgtype", "text");

        Map<String, Object> text = new HashMap<>();
        text.put("content", content);
        text.put("mentioned_mobile_list", atMobiles);
        text.put("mentioned_list", atUserIds);
        message.put("text", text);
        return message;
    }
}