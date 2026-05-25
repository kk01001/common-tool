package io.github.archer099.threadpool.thirdparty.adapter;

import io.github.archer099.threadpool.actuator.ThreadPoolMetrics;
import io.github.archer099.threadpool.thirdparty.ThirdPartyPoolType;
import io.github.archer099.threadpool.thirdparty.ThirdPartyThreadPoolConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.Connector;
import org.apache.coyote.AbstractProtocol;
import org.apache.coyote.ProtocolHandler;
import org.springframework.boot.web.embedded.tomcat.TomcatWebServer;
import org.springframework.boot.web.server.WebServer;

import java.time.LocalDateTime;
import java.util.concurrent.BlockingQueue;

/**
 * Tomcat 线程池适配器
 * 
 * <p>用于动态管理 Tomcat Web 服务器的线程池配置和监控
 * 
 * @author archer099
 */
@Slf4j
public class TomcatThreadPoolAdapter extends AbstractThirdPartyThreadPoolAdapter {
    
    private final WebServer webServer;
    private Connector connector;
    
    public TomcatThreadPoolAdapter(String poolName, WebServer webServer) {
        super(poolName, ThirdPartyPoolType.TOMCAT);
        this.webServer = webServer;
    }
    
    @Override
    public boolean isAvailable() {
        try {
            return getConnector() != null;
        } catch (Exception e) {
            log.debug("Tomcat connector not available: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    protected ThreadPoolMetrics doCollectMetrics() {
        Connector connector = getConnector();
        AbstractProtocol<?> protocol = (AbstractProtocol<?>) connector.getProtocolHandler();
        
        // 获取 Tomcat 的 Executor
        org.apache.tomcat.util.threads.ThreadPoolExecutor executor = 
            (org.apache.tomcat.util.threads.ThreadPoolExecutor) protocol.getExecutor();
        
        if (executor == null) {
            log.warn("Tomcat executor is null, cannot collect metrics");
            return null;
        }
        
        BlockingQueue<Runnable> queue = executor.getQueue();
        int queueCapacity = queue.remainingCapacity() + queue.size();
        
        return ThreadPoolMetrics.builder()
                .poolName(getPoolName())
                .corePoolSize(executor.getCorePoolSize())
                .maxPoolSize(executor.getMaximumPoolSize())
                .activeThreadCount(executor.getActiveCount())
                .poolSize(executor.getPoolSize())
                .largestPoolSize(executor.getLargestPoolSize())
                .queueCapacity(queueCapacity)
                .queueSize(queue.size())
                .queueRemainingCapacity(queue.remainingCapacity())
                .completedTaskCount(executor.getCompletedTaskCount())
                .taskCount(executor.getTaskCount())
                .rejectCount(0L) // Tomcat 不直接提供拒绝次数
                .queueUsageRatio(queueCapacity > 0 ? (double) queue.size() / queueCapacity : 0.0)
                .activeThreadRatio(executor.getMaximumPoolSize() > 0 ?
                        (double) executor.getActiveCount() / executor.getMaximumPoolSize() : 0.0)
                .shutdown(executor.isShutdown())
                .terminated(executor.isTerminated())
                .collectTime(LocalDateTime.now())
                .keepAliveTime(executor.getKeepAliveTime(java.util.concurrent.TimeUnit.SECONDS))
                .rejectedPolicyType(executor.getRejectedExecutionHandler().getClass().getSimpleName())
                .allowCoreThreadTimeout(executor.allowsCoreThreadTimeOut())
                .threadNamePrefix("tomcat-")
                .queueType(queue.getClass().getSimpleName())
                .build();
    }
    
    @Override
    protected void doUpdateConfig(ThirdPartyThreadPoolConfig config) {
        validateConfig(config);
        
        Connector connector = getConnector();
        AbstractProtocol<?> protocol = (AbstractProtocol<?>) connector.getProtocolHandler();
        
        log.info("Updating Tomcat thread pool config for [{}]", getPoolName());
        
        // 更新最大线程数
        if (config.getMaxThreads() != null) {
            int oldValue = protocol.getMaxThreads();
            protocol.setMaxThreads(config.getMaxThreads());
            log.info("Tomcat maxThreads updated: {} -> {}", oldValue, config.getMaxThreads());
        }
        
        // 更新最小空闲线程数
        if (config.getMinThreads() != null) {
            int oldValue = protocol.getMinSpareThreads();
            protocol.setMinSpareThreads(config.getMinThreads());
            log.info("Tomcat minSpareThreads updated: {} -> {}", oldValue, config.getMinThreads());
        }
        
        // 更新队列容量 (acceptCount)
        if (config.getQueueCapacity() != null) {
            int oldValue = protocol.getMaxQueueSize();
            protocol.setMaxQueueSize(config.getQueueCapacity());
            log.info("Tomcat acceptCount updated: {} -> {}", oldValue, config.getQueueCapacity());
        }
        
        // 更新最大连接数
        if (config.getMaxConnections() != null) {
            int oldValue = protocol.getMaxConnections();
            protocol.setMaxConnections(config.getMaxConnections());
            log.info("Tomcat maxConnections updated: {} -> {}", oldValue, config.getMaxConnections());
        }
        
        // 更新连接超时
        if (config.getConnectionTimeout() != null) {
            int oldValue = protocol.getConnectionTimeout();
            protocol.setConnectionTimeout(config.getConnectionTimeout().intValue());
            log.info("Tomcat connectionTimeout updated: {}ms -> {}ms", oldValue, config.getConnectionTimeout());
        }
        
        // 更新 keepAlive timeout
        if (config.getKeepAliveTime() != null) {
            int oldValue = protocol.getKeepAliveTimeout();
            protocol.setKeepAliveTimeout(config.getKeepAliveTime().intValue() / 1000);
            log.info("Tomcat keepAliveTimeout updated: {}s -> {}s", oldValue, config.getKeepAliveTime() / 1000);
        }
    }
    
    /**
     * 获取 Tomcat Connector
     */
    private Connector getConnector() {
        if (connector != null) {
            return connector;
        }
        
        try {
            if (webServer instanceof TomcatWebServer) {
                TomcatWebServer tomcatWebServer = (TomcatWebServer) webServer;
                connector = tomcatWebServer.getTomcat().getConnector();
                return connector;
            }
        } catch (Exception e) {
            log.debug("Failed to get Tomcat connector", e);
        }
        
        return null;
    }
    
    @Override
    public Object getNativeThreadPool() {
        try {
            Connector connector = getConnector();
            if (connector != null) {
                ProtocolHandler handler = connector.getProtocolHandler();
                if (handler instanceof AbstractProtocol) {
                    return ((AbstractProtocol<?>) handler).getExecutor();
                }
            }
        } catch (Exception e) {
            log.error("Failed to get native Tomcat thread pool", e);
        }
        return null;
    }
}
