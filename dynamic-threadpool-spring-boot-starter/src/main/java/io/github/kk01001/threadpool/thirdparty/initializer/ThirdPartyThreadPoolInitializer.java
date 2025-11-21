package io.github.kk01001.threadpool.thirdparty.initializer;

import io.github.kk01001.threadpool.registry.ThreadPoolRegistry;
import io.github.kk01001.threadpool.thirdparty.ThirdPartyPoolType;
import io.github.kk01001.threadpool.thirdparty.ThirdPartyThreadPoolProperties;
import io.github.kk01001.threadpool.thirdparty.adapter.TomcatThreadPoolAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;

/**
 * 第三方线程池初始化器
 * 
 * <p>在 Web 服务器初始化完成后自动注册配置的第三方线程池
 * 
 * @author kk01001
 */
@Slf4j
public class ThirdPartyThreadPoolInitializer implements ApplicationListener<WebServerInitializedEvent> {

    private final ThirdPartyThreadPoolProperties properties;
    private final ThreadPoolRegistry registry;
    
    public ThirdPartyThreadPoolInitializer(
            ThirdPartyThreadPoolProperties properties,
            ThreadPoolRegistry registry) {
        this.properties = properties;
        this.registry = registry;
    }
    
    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        if (!Boolean.TRUE.equals(properties.getEnabled())) {
            log.info("Third-party thread pool management is disabled");
            return;
        }
        
        log.info("Web server initialized, starting third-party thread pool initialization");
        
        int registered = 0;
        for (ThirdPartyThreadPoolProperties.PoolConfig poolConfig : properties.getPools()) {
            if (!Boolean.TRUE.equals(poolConfig.getEnabled())) {
                log.debug("Third-party thread pool [{}:{}] is disabled, skipping", 
                        poolConfig.getType(), poolConfig.getName());
                continue;
            }
            
            try {
                // 直接创建 Tomcat 适配器，传入 WebServer
                if (poolConfig.getType() == ThirdPartyPoolType.TOMCAT) {
                    TomcatThreadPoolAdapter adapter = new TomcatThreadPoolAdapter(poolConfig.getName(), event.getWebServer());
                    
                    if (adapter.isAvailable()) {
                        // 注册到 ThreadPoolRegistry 以便统一管理
                        registry.registerThirdPartyAdapter(adapter);
                        registered++;
                        
                        log.info("Successfully registered third-party thread pool [{}]", adapter.getPoolName());
                        
                        // 如果有初始配置，应用配置
                        if (hasInitialConfig(poolConfig)) {
                            try {
                                adapter.updateConfig(poolConfig.toConfig());
                                log.info("Applied initial configuration to [{}]", adapter.getPoolName());
                            } catch (Exception e) {
                                log.warn("Failed to apply initial configuration to [{}]: {}", 
                                        adapter.getPoolName(), e.getMessage());
                            }
                        }
                    } else {
                        log.warn("Tomcat adapter is not available for [{}]", poolConfig.getName());
                    }
                } else {
                    log.warn("Third-party pool type [{}] is not yet supported", poolConfig.getType());
                }
            } catch (Exception e) {
                log.error("Failed to register third-party thread pool [{}:{}]", 
                        poolConfig.getType(), poolConfig.getName(), e);
            }
        }
        
        log.info("Third-party thread pool initialization completed, registered {} pools", registered);
    }
    
    /**
     * 检查是否有初始配置
     */
    private boolean hasInitialConfig(ThirdPartyThreadPoolProperties.PoolConfig config) {
        return config.getMaxThreads() != null 
                || config.getMinThreads() != null
                || config.getQueueCapacity() != null
                || config.getMaxConnections() != null
                || config.getConnectionTimeout() != null
                || config.getKeepAliveTime() != null;
    }
}
