package io.github.kk01001.redis;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;
import org.redisson.config.TransportMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kk01001
 * date 2021/8/26 10:20
 * redisson 多机房配置
 */
@ComponentScan(basePackages = "io.github.kk01001.redis")
@Configuration
public class MultiRedissonConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(MultiRedissonConfig.class);

    private static final String REDIS_PROTOCOL_PREFIX = "redis://";

    private static final String REDISS_PROTOCOL_PREFIX = "rediss://";

    private static final String LINUX = "Linux";

    private final MultiRedisProperties redisProperties;

    public MultiRedissonConfig(MultiRedisProperties redisProperties) {
        this.redisProperties = redisProperties;
    }

    @Bean(name = "redissonClient")
    @Primary
    @ConditionalOnMissingBean(RedissonClient.class)
    public RedissonClient redissonClient() {
        MultiRedisProperties.Cluster cluster = redisProperties.getCluster();
        Config config = getConfig(cluster);
        config.setCodec(StringCodec.INSTANCE);
        String osName = System.getProperty("os.name");
        if (StringUtils.hasLength(osName) && osName.contains(LINUX)) {
            LOGGER.info("redisson use epoll...");
            // 传输模式 默认为TransportMode。NIO
            // 使用EPOLL传输。如果服务器绑定到环回接口，则激活unix套接字。在类路径中需要net -transport-native-epoll lib。
            config.setTransportMode(TransportMode.EPOLL);
        }
        config.setNettyThreads(cluster.getNettyThreads());
        config.setCheckLockSyncedSlaves(redisProperties.isCheckLockSyncedSlaves());
        config.setSlavesSyncTimeout(redisProperties.getSlavesSyncTimeout());
        return Redisson.create(config);
    }

    @Bean(name = "redissonClient2")
    @ConditionalOnProperty(prefix = "spring.data.redis.cluster2", name = "active", havingValue = "true")
    public RedissonClient redissonClient2() {
        MultiRedisProperties.Cluster cluster2 = redisProperties.getCluster2();
        Config config = getConfig(cluster2);
        config.setCodec(StringCodec.INSTANCE);
        String osName = System.getProperty("os.name");
        if (StringUtils.hasLength(osName) && osName.contains(LINUX)) {
            config.setTransportMode(TransportMode.EPOLL);
        }
        config.setNettyThreads(cluster2.getNettyThreads());
        config.setCheckLockSyncedSlaves(redisProperties.isCheckLockSyncedSlaves());
        config.setSlavesSyncTimeout(redisProperties.getSlavesSyncTimeout());
        return Redisson.create(config);
    }

    @Bean(name = "redissonClient3")
    @ConditionalOnProperty(prefix = "spring.data.redis.cluster3", name = "active", havingValue = "true")
    public RedissonClient redissonClient3() {
        MultiRedisProperties.Cluster cluster3 = redisProperties.getCluster3();
        Config config = getConfig(cluster3);
        config.setCodec(StringCodec.INSTANCE);
        String osName = System.getProperty("os.name");
        if (StringUtils.hasLength(osName) && osName.contains(LINUX)) {
            config.setTransportMode(TransportMode.EPOLL);
        }
        config.setNettyThreads(cluster3.getNettyThreads());
        config.setCheckLockSyncedSlaves(redisProperties.isCheckLockSyncedSlaves());
        config.setSlavesSyncTimeout(redisProperties.getSlavesSyncTimeout());
        return Redisson.create(config);
    }

    @SuppressWarnings("all")
    private Config getConfig(MultiRedisProperties.Cluster cluster) {
        String clusterPassword = cluster.getPassword();
        String password = StringUtils.hasLength(clusterPassword) ? clusterPassword : redisProperties.getPassword();
        List<String> nodesList = cluster.getNodes();
        String[] nodes = convert(nodesList);

        Config config = new Config();
        config.useClusterServers()
                // 添加Redis集群节点地址。使用以下格式——host:port
                .addNodeAddress(nodes)
                // 连接任何Redis服务器时超时。缺省值是10000毫秒。
                .setConnectTimeout(redisProperties.getConnectionTimeout())
                // Redis服务器响应超时。成功发送Redis命令后开始倒计时。默认为3000毫秒
                .setTimeout(redisProperties.getResponseTimeout())
                // 如果池连接在超时时间内未使用，且当前连接数量大于最小空闲连接池大小，则将关闭该连接并将其从池中移除。缺省值是10000毫秒。
                .setIdleConnectionTimeout(redisProperties.getIdleConnectionTimeout())
                // 如果Redis命令在重试尝试后不能发送到Redis服务器，将抛出错误。但是如果它发送成功，那么超时将开始。 默认为3次尝试
                .setRetryAttempts(redisProperties.getRetryAttempts())
                // 定义另一个尝试发送Redis命令的时间间隔，如果它还没有发送。 默认为1500毫秒
                .setRetryInterval(redisProperties.getRetryInterval())
                // Redis 'master'节点最大连接池大小 。 默认值是64
                .setMasterConnectionPoolSize(redisProperties.getMasterConnectionPoolSize())
                // 在Redisson启动期间启用集群槽位检查。 默认为true
                .setCheckSlotsCoverage(redisProperties.isCheckSlotsCoverage())
                // Redis集群扫描间隔(毫秒) 默认为5000
                .setScanInterval(redisProperties.getScanInterval())
                // 设置用于读操作的节点类型。 默认为SLAVE
                .setReadMode(redisProperties.getReadMode())
                // Redis '从'节点每个从节点的最大连接池大小  默认值是64
                .setSlaveConnectionPoolSize(redisProperties.getSlaveConnectionPoolSize())
                // Redis鉴权密码。如果不需要，应该为空。
                .setPassword(password);
        return config;
    }

    @SuppressWarnings("all")
    private String[] convert(List<String> nodesObject) {
        List<String> nodes = new ArrayList<>(nodesObject.size());
        for (String node : nodesObject) {
            if (!node.startsWith(REDIS_PROTOCOL_PREFIX) && !node.startsWith(REDISS_PROTOCOL_PREFIX)) {
                nodes.add(REDIS_PROTOCOL_PREFIX + node);
            } else {
                nodes.add(node);
            }
        }
        return nodes.toArray(new String[nodes.size()]);
    }

}
