package io.github.kk01001.sftp.core;

import com.jcraft.jsch.ChannelSftp;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.util.Assert;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author linshiqiang
 * date 2023-03-29 20:45:00
 */
@Slf4j
public class JschConnectionPool {

    private final GenericObjectPool<ChannelSftp> defaultPool;

    private final ReentrantLock lock = new ReentrantLock();

    private final Map</*sftp key*/String, /*连接池*/GenericObjectPool<ChannelSftp>> poolMap = new ConcurrentHashMap<>(16);

    public void buildPool(String poolKey, SftpInfoProperties sftpInfoProperties) {
        lock.lock();
        try {
            GenericObjectPool<ChannelSftp> pool = poolMap.get(poolKey);
            if (Objects.nonNull(pool)) {
                pool.close();
                GenericObjectPool<ChannelSftp> genericObjectPool = getPool(sftpInfoProperties);
                poolMap.put(poolKey, genericObjectPool);
                return;
            }
            GenericObjectPool<ChannelSftp> genericObjectPool = getPool(sftpInfoProperties);
            poolMap.put(poolKey, genericObjectPool);
        } finally {
            lock.unlock();
        }
    }

    private GenericObjectPool<ChannelSftp> getPool(SftpInfoProperties sftpInfoProperties) {
        return buildJschConnectionPool(sftpInfoProperties.getHost(),
                sftpInfoProperties.getPort(),
                sftpInfoProperties.getUsername(),
                sftpInfoProperties.getPassword(),
                sftpInfoProperties.getMaxTotal(),
                sftpInfoProperties.getMinIdle(),
                sftpInfoProperties.getMaxIdle(),
                sftpInfoProperties.getConnectTimeout(),
                sftpInfoProperties.getMinEvictableIdleTime());
    }

    public GenericObjectPool<ChannelSftp> buildJschConnectionPool(String host,
                                                                  int port,
                                                                  String username,
                                                                  String password,
                                                                  Integer maxTotal,
                                                                  Integer minIdle,
                                                                  Integer maxIdle,
                                                                  Duration timout,
                                                                  Duration minEvictableIdleTime) {
        JschFactory jschFactory = new JschFactory(host, port, username, password);
        GenericObjectPoolConfig<ChannelSftp> config = new GenericObjectPoolConfig<>();

        // 设置最大连接数
        config.setMaxTotal(maxTotal);

        // 设置空闲连接数
        config.setMinIdle(minIdle);

        // 设置最大空闲连接数
        config.setMaxIdle(maxIdle);

        // 设置连接等待时间，单位毫秒
        config.setMaxWait(timout);
        config.setTestOnReturn(true);
        config.setTestOnBorrow(true);
        config.setTestOnCreate(true);
        config.setBlockWhenExhausted(true);
        config.setTestWhileIdle(true);
        config.setMinEvictableIdleDuration(minEvictableIdleTime);
        return new GenericObjectPool<>(jschFactory, config);
    }

    public JschConnectionPool(String host,
                              int port,
                              String username,
                              String password,
                              Integer maxTotal,
                              Integer minIdle,
                              Integer maxIdle,
                              Duration timout,
                              Duration minEvictableIdleTime) {
        JschFactory jschFactory = new JschFactory(host, port, username, password);
        GenericObjectPoolConfig<ChannelSftp> config = new GenericObjectPoolConfig<>();

        // 设置最大连接数
        config.setMaxTotal(maxTotal);

        // 设置空闲连接数
        config.setMinIdle(minIdle);

        // 设置最大空闲连接数
        config.setMaxIdle(maxIdle);

        // 设置连接等待时间，单位毫秒
        config.setMaxWait(timout);
        config.setTestOnReturn(true);
        config.setTestOnBorrow(true);
        config.setTestOnCreate(true);
        config.setBlockWhenExhausted(true);
        config.setTestWhileIdle(true);
        config.setMinEvictableIdleDuration(minEvictableIdleTime);
        defaultPool = new GenericObjectPool<>(jschFactory, config);
    }

    public ChannelSftp borrowObject() throws Exception {
        ChannelSftp channelSftp = defaultPool.borrowObject();
        log.info("borrow pool active: {}, idle: {}", defaultPool.getNumActive(), defaultPool.getNumIdle());
        return channelSftp;
    }

    public ChannelSftp borrowObject(String poolKey) throws Exception {
        GenericObjectPool<ChannelSftp> pool = poolMap.get(poolKey);
        Assert.notNull(pool, "sftp pool is null");
        ChannelSftp channelSftp = pool.borrowObject();
        log.info("poolKey: {}, borrow pool active: {}, idle: {}", poolKey, pool.getNumActive(), pool.getNumIdle());
        return channelSftp;
    }

    public void returnObject(ChannelSftp channelSftp) {
        if (channelSftp == null) {
            return;
        }
        if (channelSftp.isConnected()) {
            defaultPool.returnObject(channelSftp);
        }
        log.info("return pool active: {}, idle: {}", defaultPool.getNumActive(), defaultPool.getNumIdle());
    }

    public void returnObject(String poolKey, ChannelSftp channelSftp) {
        if (channelSftp == null) {
            return;
        }
        GenericObjectPool<ChannelSftp> pool = poolMap.get(poolKey);
        Assert.notNull(pool, "sftp pool is null");

        if (channelSftp.isConnected()) {
            pool.returnObject(channelSftp);
        }
        log.info("poolKey: {}, return pool active: {}, idle: {}", poolKey, pool.getNumActive(), pool.getNumIdle());
    }

    public void close() {
        defaultPool.close();
    }

    public void close(String poolKey) {
        GenericObjectPool<ChannelSftp> pool = poolMap.get(poolKey);
        Assert.notNull(pool, "sftp pool is null");
        pool.close();
    }
}
