package io.github.kk01001.robot.message;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class LinkMessage implements RobotMessage {
    private String title;
    private String text;
    private String messageUrl;
    private String picUrl;
    
    @Override
    public String getType() {
        return "link";
    }
    
    @Override
    public Map<String, Object> toMessageMap() {
        Map<String, Object> message = new HashMap<>();
        message.put("msgtype", "link");
        
        Map<String, Object> link = new HashMap<>();
        link.put("title", title);
        link.put("text", text);
        link.put("messageUrl", messageUrl);
        link.put("picUrl", picUrl);
        message.put("link", link);
        
        return message;
    }

    @Override
    public Map<String, Object> toMessageMap(String robotType) {
        return Map.of();
    }
}