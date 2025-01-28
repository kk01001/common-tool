package io.github.kk01001.robot.message;

import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class MarkdownMessage implements RobotMessage {
    private String title;
    private String content;
    private List<String> atMobiles;
    private List<String> atUserIds;
    private Boolean atAll;
    
    @Override
    public String getType() {
        return "markdown";
    }
    
    @Override
    public Map<String, Object> toMessageMap() {
        Map<String, Object> message = new HashMap<>();
        message.put("msgtype", "markdown");
        
        Map<String, Object> markdown = new HashMap<>();
        markdown.put("title", title);
        markdown.put("text", content);
        message.put("markdown", markdown);
        
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
        message.put("msgtype", "markdown");

        Map<String, Object> text = new HashMap<>();
        text.put("content", content);
        message.put("markdown", text);
        return message;
    }
}