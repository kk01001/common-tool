package io.github.kk01001.robot.message;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class ImageMessage implements RobotMessage {
    private String base64;
    private String md5;
    
    @Override
    public String getType() {
        return "image";
    }
    
    @Override
    public Map<String, Object> toMessageMap() {
        Map<String, Object> message = new HashMap<>();
        message.put("msgtype", "image");
        
        Map<String, Object> image = new HashMap<>();
        image.put("base64", base64);
        image.put("md5", md5);
        message.put("image", image);
        
        return message;
    }

    @Override
    public Map<String, Object> toMessageMap(String robotType) {
        return toMessageMap();
    }
}