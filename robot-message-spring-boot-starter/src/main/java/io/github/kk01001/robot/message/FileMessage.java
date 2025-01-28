package io.github.kk01001.robot.message;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 文件消息
 * 用于发送文件类型的消息
 */
@Data
public class FileMessage implements RobotMessage {
    
    /**
     * 文件媒体ID
     * 通过文件上传接口获取
     */
    private String mediaId;
    
    @Override
    public String getType() {
        return "file";
    }
    
    @Override
    public Map<String, Object> toMessageMap() {
        Map<String, Object> message = new HashMap<>();
        message.put("msgtype", "file");
        
        Map<String, Object> file = new HashMap<>();
        file.put("media_id", mediaId);
        message.put("file", file);
        
        return message;
    }
    
    @Override
    public Map<String, Object> toMessageMap(String robotType) {
        if ("wechat".equals(robotType)) {
            return toMessageMap();
        }
        // 钉钉等其他机器人暂不支持文件消息
        return Map.of();
    }
} 