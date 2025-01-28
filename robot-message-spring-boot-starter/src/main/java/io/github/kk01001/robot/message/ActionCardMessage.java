package io.github.kk01001.robot.message;

import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class ActionCardMessage implements RobotMessage {
    private String title;
    private String text;
    private String btnOrientation; // 0-按钮竖直排列，1-按钮横向排列
    private String singleTitle; // 单个按钮的标题
    private String singleURL; // 单个按钮的跳转链接
    private List<Button> btns; // 多个按钮的配置
    
    @Data
    public static class Button {
        private String title;
        private String actionURL;
    }
    
    @Override
    public String getType() {
        return "actionCard";
    }
    
    @Override
    public Map<String, Object> toMessageMap() {
        Map<String, Object> message = new HashMap<>();
        message.put("msgtype", "actionCard");
        
        Map<String, Object> actionCard = new HashMap<>();
        actionCard.put("title", title);
        actionCard.put("text", text);
        actionCard.put("btnOrientation", btnOrientation);
        
        if (btns != null && !btns.isEmpty()) {
            actionCard.put("btns", btns);
        } else {
            actionCard.put("singleTitle", singleTitle);
            actionCard.put("singleURL", singleURL);
        }
        
        message.put("actionCard", actionCard);
        return message;
    }
} 