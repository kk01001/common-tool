package io.github.archer099.redisson.enums;

/**
 * @author archer099
 * @date 2026-01-15 16:00:00
 * @description Redis 读取模式
 */
public enum RedissonReadMode {

    /**
     * 从从节点读取（默认）
     * 读取操作将发送到从节点，主节点仅用于写入
     */
    SLAVE,

    /**
     * 从主节点读取
     * 所有读取操作都发送到主节点
     */
    MASTER,

    /**
     * 从主从节点读取
     * 读取操作会在主节点和从节点之间负载均衡
     */
    MASTER_SLAVE
}
