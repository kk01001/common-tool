package io.github.archer099.redisson.enums;

/**
 * @author archer099
 * @date 2026-01-15 15:10:00
 * @description 故障节点检测器类型
 */
public enum FailedNodeDetectorType {

    /**
     * 基于连接错误检测（默认）
     * 在 checkInterval 时间内有持续的连接错误则标记为故障
     */
    CONNECTION,

    /**
     * 基于命令执行错误数量检测
     * 在 checkInterval 时间内命令执行错误达到 failedCommandsLimit 则标记为故障
     */
    COMMANDS,

    /**
     * 基于命令超时错误数量检测
     * 在 checkInterval 时间内命令超时错误达到 failedCommandsLimit 则标记为故障
     */
    COMMANDS_TIMEOUT
}
