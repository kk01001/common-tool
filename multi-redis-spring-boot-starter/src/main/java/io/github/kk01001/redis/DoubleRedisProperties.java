package io.github.kk01001.redis;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.redisson.config.ReadMode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

/**
 * @author kk01001
 * date 2021/8/26 10:15
 */
@Component
@JsonIgnoreProperties(ignoreUnknown = true)
@ConfigurationProperties(prefix = "spring.data.redis")
public class DoubleRedisProperties implements Serializable {

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

    public ReadMode getReadMode() {
        return readMode;
    }

    public void setReadMode(ReadMode readMode) {
        this.readMode = readMode;
    }

    public int getScanInterval() {
        return scanInterval;
    }

    public void setScanInterval(int scanInterval) {
        this.scanInterval = scanInterval;
    }

    public boolean isCheckSlotsCoverage() {
        return checkSlotsCoverage;
    }

    public void setCheckSlotsCoverage(boolean checkSlotsCoverage) {
        this.checkSlotsCoverage = checkSlotsCoverage;
    }

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

        public int getNettyThreads() {
            return nettyThreads;
        }

        public void setNettyThreads(int nettyThreads) {
            this.nettyThreads = nettyThreads;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public List<String> getNodes() {
            return nodes;
        }

        public void setNodes(List<String> nodes) {
            this.nodes = nodes;
        }

        public Integer getMaxRedirects() {
            return maxRedirects;
        }

        public void setMaxRedirects(Integer maxRedirects) {
            this.maxRedirects = maxRedirects;
        }

        public Boolean getActive() {
            return active;
        }

        public void setActive(Boolean active) {
            this.active = active;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }
    }

    public static class Jedis {

        /**
         * Jedis pool configuration.
         */
        private Pool pool;

        public Pool getPool() {
            return pool;
        }

        public void setPool(Pool pool) {
            this.pool = pool;
        }
    }

    public static class Pool {

        private int maxIdle = 50;

        private int minIdle = 10;

        private int maxActive = 200;

        private long maxWait = -1;

        private long timeBetweenEvictionRuns = -1;

        public int getMaxIdle() {
            return maxIdle;
        }

        public void setMaxIdle(int maxIdle) {
            this.maxIdle = maxIdle;
        }

        public int getMinIdle() {
            return minIdle;
        }

        public void setMinIdle(int minIdle) {
            this.minIdle = minIdle;
        }

        public int getMaxActive() {
            return maxActive;
        }

        public void setMaxActive(int maxActive) {
            this.maxActive = maxActive;
        }

        public long getMaxWait() {
            return maxWait;
        }

        public void setMaxWait(long maxWait) {
            this.maxWait = maxWait;
        }

        public long getTimeBetweenEvictionRuns() {
            return timeBetweenEvictionRuns;
        }

        public void setTimeBetweenEvictionRuns(long timeBetweenEvictionRuns) {
            this.timeBetweenEvictionRuns = timeBetweenEvictionRuns;
        }
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(Integer connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getResponseTimeout() {
        return responseTimeout;
    }

    public void setResponseTimeout(int responseTimeout) {
        this.responseTimeout = responseTimeout;
    }

    public int getIdleConnectionTimeout() {
        return idleConnectionTimeout;
    }

    public void setIdleConnectionTimeout(int idleConnectionTimeout) {
        this.idleConnectionTimeout = idleConnectionTimeout;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
    }

    public Cluster getCluster2() {
        return cluster2;
    }

    public void setCluster2(Cluster cluster2) {
        this.cluster2 = cluster2;
    }

    public Cluster getCluster3() {
        return cluster3;
    }

    public void setCluster3(Cluster cluster3) {
        this.cluster3 = cluster3;
    }

    public Integer getMasterConnectionPoolSize() {
        return masterConnectionPoolSize;
    }

    public void setMasterConnectionPoolSize(Integer masterConnectionPoolSize) {
        this.masterConnectionPoolSize = masterConnectionPoolSize;
    }

    public Jedis getJedis() {
        return jedis;
    }

    public void setJedis(Jedis jedis) {
        this.jedis = jedis;
    }

    public int getSlaveConnectionPoolSize() {
        return slaveConnectionPoolSize;
    }

    public void setSlaveConnectionPoolSize(int slaveConnectionPoolSize) {
        this.slaveConnectionPoolSize = slaveConnectionPoolSize;
    }

    public int getRetryAttempts() {
        return retryAttempts;
    }

    public void setRetryAttempts(int retryAttempts) {
        this.retryAttempts = retryAttempts;
    }

    public int getRetryInterval() {
        return retryInterval;
    }

    public void setRetryInterval(int retryInterval) {
        this.retryInterval = retryInterval;
    }
}
