package io.github.kk01001.redisson.properties;

import io.github.kk01001.redisson.enums.FailedNodeDetectorType;
import lombok.Getter;
import lombok.Setter;

/**
 * @author kk01001
 * @date 2026-01-15 15:10:00
 * @description 故障节点检测器配置
 */
@Getter
@Setter
public class FailedNodeDetectorProperties {

    /**
     * 检测器类型
     * CONNECTION: 基于连接错误检测（默认）
     * COMMANDS: 基于命令执行错误数量检测
     * COMMANDS_TIMEOUT: 基于命令超时错误数量检测
     */
    private FailedNodeDetectorType type = FailedNodeDetectorType.CONNECTION;

    /**
     * 检查间隔（毫秒）
     * 在此时间窗口内统计错误
     */
    private long checkInterval = 180000;

    /**
     * 失败命令数量限制
     * 仅对 COMMANDS 和 COMMANDS_TIMEOUT 类型有效
     * 达到此数量后标记节点为故障
     */
    private int failedCommandsLimit = 3;
}
