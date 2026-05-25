package io.github.archer099.redisson.properties;

import io.github.archer099.redisson.enums.RedissonMode;
import lombok.Getter;
import lombok.Setter;

/**
 * @author archer099
 * @date 2026-01-15 10:00:00
 * @description 单个 Redisson 实例配置
 */
@Setter
@Getter
public class RedissonInstanceProperties {

    /**
     * Redis 部署模式
     */
    private RedissonMode mode = RedissonMode.SINGLE;

    /**
     * 单机模式配置
     */
    private SingleServerProperties single;

    /**
     * 哨兵模式配置
     */
    private SentinelServerProperties sentinel;

    /**
     * 主从模式配置
     */
    private MasterSlaveServerProperties masterSlave;

    /**
     * 集群模式配置
     */
    private ClusterServerProperties cluster;

    // ========== 通用配置 ==========

    /**
     * 用于 Redis 连接的线程数
     */
    private int threads = 16;

    /**
     * 用于 Netty 的线程数
     */
    private int nettyThreads = 32;

    /**
     * 编解码器全类名
     */
    private String codec = "org.redisson.client.codec.StringCodec";

    /**
     * 传输模式：NIO, EPOLL, KQUEUE
     */
    private String transportMode = "NIO";

    /**
     * 是否检查锁的同步从节点
     */
    private boolean checkLockSyncedSlaves = false;

    /**
     * 从节点同步超时时间（毫秒）
     */
    private long slavesSyncTimeout = 1000;

    /**
     * 锁看门狗超时时间（毫秒）
     */
    private long lockWatchdogTimeout = 30000;

    /**
     * 是否保持 PubSub 订阅顺序
     */
    private boolean keepPubSubOrder = true;

    /**
     * 是否使用脚本缓存
     */
    private boolean useScriptCache = false;

    /**
     * 最小清理延迟（秒）
     */
    private int minCleanUpDelay = 5;

    /**
     * 最大清理延迟（秒）
     */
    private int maxCleanUpDelay = 1800;

    /**
     * 清理关闭延迟（秒）
     */
    private int cleanUpKeysAmount = 100;

    /**
     * 可靠 Topic 看门狗超时时间（毫秒）
     */
    private long reliableTopicWatchdogTimeout = 600000;

    /**
     * 地址解析组超时时间（毫秒）
     */
    private long addressResolverGroupFactory = 15000;
}
