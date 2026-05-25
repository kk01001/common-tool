package io.github.archer099.threadpool.thirdparty.adapter;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import io.github.archer099.threadpool.actuator.ThreadPoolMetrics;
import io.github.archer099.threadpool.thirdparty.ThirdPartyPoolType;
import io.github.archer099.threadpool.thirdparty.ThirdPartyThreadPoolConfig;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.time.LocalDateTime;

/**
 * Hikari 连接池适配器
 *
 * <p>
 * 用于动态管理 Hikari 数据库连接池的配置和监控
 *
 * @author archer099
 */
@Slf4j
public class HikariThreadPoolAdapter extends AbstractThirdPartyThreadPoolAdapter {

    private final DataSource dataSource;
    private HikariDataSource hikariDataSource;

    public HikariThreadPoolAdapter(String poolName, DataSource dataSource) {
        super(poolName, ThirdPartyPoolType.HIKARI);
        this.dataSource = dataSource;
    }

    @Override
    public boolean isAvailable() {
        try {
            return getHikariDataSource() != null;
        } catch (Exception e) {
            log.debug("Hikari DataSource not available: {}", e.getMessage());
            return false;
        }
    }

    @Override
    protected ThreadPoolMetrics doCollectMetrics() {
        HikariDataSource hikari = getHikariDataSource();
        HikariPoolMXBean poolMXBean = hikari.getHikariPoolMXBean();

        if (poolMXBean == null) {
            log.warn("Hikari PoolMXBean is null, cannot collect metrics");
            return null;
        }

        // Hikari 连接池指标
        int totalConnections = poolMXBean.getTotalConnections();
        int activeConnections = poolMXBean.getActiveConnections();
        int idleConnections = poolMXBean.getIdleConnections();
        int threadsAwaitingConnection = poolMXBean.getThreadsAwaitingConnection();

        // 配置信息
        int maximumPoolSize = hikari.getMaximumPoolSize();
        int minimumIdle = hikari.getMinimumIdle();

        return ThreadPoolMetrics.builder()
                .poolName(getPoolName())
                .corePoolSize(minimumIdle)
                .maxPoolSize(maximumPoolSize)
                .activeThreadCount(activeConnections)
                .poolSize(totalConnections)
                .largestPoolSize(totalConnections) // Hikari 不提供历史最大值，使用当前值
                .queueCapacity(maximumPoolSize) // 使用最大连接数作为容量
                .queueSize(threadsAwaitingConnection) // 等待连接的线程数
                .queueRemainingCapacity(maximumPoolSize - totalConnections)
                .completedTaskCount(0L) // Hikari 不提供已完成任务数
                .taskCount(0L) // Hikari 不提供总任务数
                .rejectCount(0L) // Hikari 不直接提供拒绝次数
                .queueUsageRatio(maximumPoolSize > 0 ? (double) threadsAwaitingConnection / maximumPoolSize : 0.0)
                .activeThreadRatio(maximumPoolSize > 0 ? (double) activeConnections / maximumPoolSize : 0.0)
                .shutdown(hikari.isClosed())
                .terminated(hikari.isClosed())
                .collectTime(LocalDateTime.now())
                .keepAliveTime(hikari.getIdleTimeout() / 1000) // 转换为秒
                .rejectedPolicyType("N/A")
                .allowCoreThreadTimeout(false)
                .threadNamePrefix("hikari-")
                .queueType("HikariPool")
                .build();
    }

    @Override
    protected void doUpdateConfig(ThirdPartyThreadPoolConfig config) {
        validateConfig(config);

        HikariDataSource hikari = getHikariDataSource();
        assert hikari != null;

        log.info("Updating Hikari connection pool config for [{}]", getPoolName());

        // 更新最大连接数
        if (config.getMaxThreads() != null) {
            int oldValue = hikari.getMaximumPoolSize();
            hikari.setMaximumPoolSize(config.getMaxThreads());
            log.info("Hikari maximumPoolSize updated: {} -> {}", oldValue, config.getMaxThreads());
        }

        // 更新最小空闲连接数
        if (config.getMinThreads() != null) {
            int oldValue = hikari.getMinimumIdle();
            hikari.setMinimumIdle(config.getMinThreads());
            log.info("Hikari minimumIdle updated: {} -> {}", oldValue, config.getMinThreads());
        }

        // 更新连接超时时间
        if (config.getConnectionTimeout() != null) {
            long oldValue = hikari.getConnectionTimeout();
            hikari.setConnectionTimeout(config.getConnectionTimeout());
            log.info("Hikari connectionTimeout updated: {}ms -> {}ms", oldValue, config.getConnectionTimeout());
        }

        // 更新空闲超时时间 (keepAliveTime 映射到 idleTimeout)
        if (config.getKeepAliveTime() != null) {
            long oldValue = hikari.getIdleTimeout();
            hikari.setIdleTimeout(config.getKeepAliveTime());
            log.info("Hikari idleTimeout updated: {}ms -> {}ms", oldValue, config.getKeepAliveTime());
        }

        // 更新连接最大生命周期
        if (config.getMaxLifetime() != null) {
            long oldValue = hikari.getMaxLifetime();
            hikari.setMaxLifetime(config.getMaxLifetime());
            log.info("Hikari maxLifetime updated: {}ms -> {}ms", oldValue, config.getMaxLifetime());
        }
    }

    /**
     * 获取 HikariDataSource
     */
    private HikariDataSource getHikariDataSource() {
        if (hikariDataSource != null) {
            return hikariDataSource;
        }

        try {
            if (dataSource instanceof HikariDataSource) {
                hikariDataSource = (HikariDataSource) dataSource;
                return hikariDataSource;
            } else {
                log.warn("DataSource is not a HikariDataSource: {}", dataSource.getClass().getName());
            }
        } catch (Exception e) {
            log.debug("Failed to get HikariDataSource", e);
        }

        return null;
    }

    @Override
    public Object getNativeThreadPool() {
        return getHikariDataSource();
    }
}
