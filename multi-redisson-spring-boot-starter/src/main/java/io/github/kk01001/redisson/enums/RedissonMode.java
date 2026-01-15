package io.github.kk01001.redisson.enums;

/**
 * @author kk01001
 * @date 2026-01-15 10:00:00
 * @description Redis 部署模式枚举
 */
public enum RedissonMode {

    /**
     * 单机模式
     */
    SINGLE,

    /**
     * 哨兵模式
     */
    SENTINEL,

    /**
     * 主从模式
     */
    MASTER_SLAVE,

    /**
     * 集群模式
     */
    CLUSTER
}
