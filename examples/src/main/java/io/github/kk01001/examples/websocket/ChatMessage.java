package io.github.kk01001.examples.websocket;

import lombok.Data;

@Data
public class ChatMessage {
    /**
     * 消息类型
     */
    private MessageType type;
    
    /**
     * 发送者ID
     */
    private String fromSessionId;
    
    /**
     * 接收者ID（私聊时使用）
     */
    private String toSessionId;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 时间戳
     */
    private long timestamp = System.currentTimeMillis();
    
    public enum MessageType {
        /**
         * 系统消息
         */
        SYSTEM,
        
        /**
         * 用户加入
         */
        JOIN,
        
        /**
         * 用户离开
         */
        LEAVE,
        
        /**
         * 广播消息
         */
        BROADCAST,
        
        /**
         * 私聊消息
         */
        PRIVATE
    }
} 