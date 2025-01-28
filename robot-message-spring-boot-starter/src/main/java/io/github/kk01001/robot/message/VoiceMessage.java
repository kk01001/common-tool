package io.github.kk01001.robot.message;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 语音消息
 * 用于发送语音类型的消息
 */
@Data
public class VoiceMessage implements RobotMessage {
    
    /**
     * 语音媒体ID
     * 通过语音文件上传接口获取
     */
    private String mediaId;
    
    @Override
    public String getType() {
        return "voice";
    }
    
    @Override
    public Map<String, Object> toMessageMap() {
        Map<String, Object> message = new HashMap<>();
        message.put("msgtype", "voice");
        
        Map<String, Object> voice = new HashMap<>();
        voice.put("media_id", mediaId);
        message.put("voice", voice);
        
        return message;
    }
    
    @Override
    public Map<String, Object> toMessageMap(String robotType) {
        if ("wechat".equals(robotType)) {
            return toMessageMap();
        }
        // 钉钉等其他机器人暂不支持语音消息
        return Map.of();
    }
} 