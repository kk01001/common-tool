package io.github.kk01001.robot.message;

import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class FeedCardMessage implements RobotMessage {
    private List<FeedCardLink> links;
    
    @Data
    public static class FeedCardLink {
        private String title;
        private String messageURL;
        private String picURL;
    }
    
    @Override
    public String getType() {
        return "feedCard";
    }
    
    @Override
    public Map<String, Object> toMessageMap() {
        Map<String, Object> message = new HashMap<>();
        message.put("msgtype", "feedCard");
        
        Map<String, Object> feedCard = new HashMap<>();
        feedCard.put("links", links);
        message.put("feedCard", feedCard);
        
        return message;
    }

    @Override
    public Map<String, Object> toMessageMap(String robotType) {
        return Map.of();
    }
}