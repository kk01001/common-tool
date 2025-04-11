package io.github.kk01001.redis;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import org.redisson.config.ReadMode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

/**
 * @author kk01001
 * date 2021/8/26 10:15
 */
@Setter
@Getter
@Component
@JsonIgnoreProperties(ignoreUnknown = true)
@ConfigurationProperties(prefix = "spring.data.redis")
public class MultiRedisProperties implements Serializable {

    /**
     * Login password of the redis server.
     */
    private String password;

    /**
     * Connection timeout.
     */
    private Integer connectionTimeout = 5000;

    /**
     * Redis server response timeout. Starts to countdown when Redis command was succesfully sent.
     * Value in milliseconds.
     */
    private int responseTimeout = 3000;

    /**
     * If pooled connection not used for a <code>timeout</code> time
     * and current connections amount bigger than minimum idle connections pool size,
     * then it will closed and removed from pool.
     * Value in milliseconds.
     */
    private int idleConnectionTimeout = 10000;

    /**
     * Defines whether to check synchronized slaves amount
     * with actual slaves amount after lock acquisition.
     * <p>
     * Default is <code>true</code>.
     *
     * @param checkLockSyncedSlaves <code>true</code> if check required,
     * <code>false</code> otherwise.
     * @return config
     */
    private boolean checkLockSyncedSlaves = false;

    /**
     * Defines slaves synchronization timeout applied to each operation of {@link org.redisson.api.RLock},
     * {@link org.redisson.api.RSemaphore}, {@link org.redisson.api.RPermitExpirableSemaphore} objects.
     * <p>
     * Default is <code>1000</code> milliseconds.
     *
     * @param timeout timeout in milliseconds
     * @return config
     */
    private long slavesSyncTimeout = 1000;


    /**
     * 本地集群
     */
    private Cluster cluster = new Cluster();

    /**
     * 异地集群
     */
    private Cluster cluster2 = new Cluster();

    private Cluster cluster3 = new Cluster();

    private Integer masterConnectionPoolSize = 100;

    /**
     * Redis 'slave' node maximum connection pool size for <b>each</b> slave node
     */
    private int slaveConnectionPoolSize = 128;

    private int retryAttempts = 3;

    private int retryInterval = 1000;

    private ReadMode readMode = ReadMode.MASTER;

    private int scanInterval = 5000;

    private boolean checkSlotsCoverage = true;

    /**
     * jedis 连接池
     */
    private Jedis jedis;

    @Setter
    @Getter
    public static class Cluster implements Serializable {

        private int nettyThreads = 32;

        /**
         * redis密码, cluster优先
         */
        private String password;

        /**
         * Comma-separated list of "host:port" pairs to bootstrap from. This represents an
         * "initial" list of cluster nodes and is required to have at least one entry.
         */
        private List<String> nodes;

        /**
         * Maximum number of redirects to follow when executing commands across the
         * cluster.
         */
        private Integer maxRedirects = 3;

        /**
         * 是否激活
         */
        private Boolean active = false;

        /**
         * 机房位置 A/B
         */
        private String location;

    }

    @Setter
    @Getter
    public static class Jedis {

        /**
         * Jedis pool configuration.
         */
        private Pool pool;

    }

    @Setter
    @Getter
    public static class Pool {

        private int maxIdle = 50;

        private int minIdle = 10;

        private int maxActive = 200;

        private long maxWait = -1;

        private long timeBetweenEvictionRuns = -1;

    }

}
