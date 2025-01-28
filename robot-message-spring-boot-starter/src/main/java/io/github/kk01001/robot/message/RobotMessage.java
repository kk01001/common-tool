package io.github.kk01001.robot.message;

import java.util.Map;

/**
 * 机器人消息接口
 * 所有类型的消息都需要实现此接口
 */
public interface RobotMessage {
    
    /**
     * 获取消息类型
     * @return 消息类型标识符
     */
    String getType();

    /**
     * 将消息转换为Map格式
     * @return 包含消息内容的Map对象
     */
    Map<String, Object> toMessageMap();

    /**
     * 将消息转换为Map格式
     *
     * @return 包含消息内容的Map对象
     */
    Map<String, Object> toMessageMap(String robotType);
} 