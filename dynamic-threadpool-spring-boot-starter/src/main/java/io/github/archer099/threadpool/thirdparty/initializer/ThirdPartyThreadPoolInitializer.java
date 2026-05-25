package io.github.archer099.threadpool.thirdparty.initializer;

import io.github.archer099.threadpool.registry.ThreadPoolRegistry;
import io.github.archer099.threadpool.thirdparty.ThirdPartyPoolType;
import io.github.archer099.threadpool.thirdparty.ThirdPartyThreadPoolProperties;
import io.github.archer099.threadpool.thirdparty.adapter.HikariThreadPoolAdapter;
import io.github.archer099.threadpool.thirdparty.adapter.ThirdPartyThreadPoolAdapter;
import io.github.archer099.threadpool.thirdparty.adapter.TomcatThreadPoolAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.boot.web.server.WebServer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;

import javax.sql.DataSource;

/**
 * 第三方线程池初始化器
 * 
 * <p>
 * 在 Web 服务器初始化完成后自动注册配置的第三方线程池
 * 
 * @author archer099
 */
@Slf4j
public class ThirdPartyThreadPoolInitializer implements ApplicationListener<WebServerInitializedEvent> {

    private final ThirdPartyThreadPoolProperties properties;
    private final ThreadPoolRegistry registry;
    private final ApplicationContext applicationContext;

    public ThirdPartyThreadPoolInitializer(
            ThirdPartyThreadPoolProperties properties,
            ThreadPoolRegistry registry,
            ApplicationContext applicationContext) {
        this.properties = properties;
        this.registry = registry;
        this.applicationContext = applicationContext;
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
                ThirdPartyThreadPoolAdapter adapter = createAdapter(poolConfig, event.getWebServer());
                if (adapter == null) {
                    continue;
                }

                registerAdapter(adapter, poolConfig);
                registered++;
            } catch (Exception e) {
                log.error("Failed to register third-party thread pool [{}:{}]",
                        poolConfig.getType(), poolConfig.getName(), e);
            }
        }

        log.info("Third-party thread pool initialization completed, registered {} pools", registered);
    }

    /**
     * 根据配置创建适配器
     */
    private ThirdPartyThreadPoolAdapter createAdapter(
            ThirdPartyThreadPoolProperties.PoolConfig poolConfig,
            WebServer webServer) {

        ThirdPartyPoolType type = poolConfig.getType();

        if (type == ThirdPartyPoolType.TOMCAT) {
            return createTomcatAdapter(poolConfig, webServer);
        }

        if (type == ThirdPartyPoolType.HIKARI) {
            return createHikariAdapter(poolConfig);
        }

        log.warn("Third-party pool type [{}] is not yet supported", type);
        return null;
    }

    /**
     * 创建 Tomcat 适配器
     */
    private ThirdPartyThreadPoolAdapter createTomcatAdapter(
            ThirdPartyThreadPoolProperties.PoolConfig poolConfig,
            WebServer webServer) {

        TomcatThreadPoolAdapter adapter = new TomcatThreadPoolAdapter(poolConfig.getName(), webServer);

        if (!adapter.isAvailable()) {
            log.warn("Tomcat adapter is not available for [{}]", poolConfig.getName());
            return null;
        }

        log.info("Successfully created Tomcat adapter [{}]", adapter.getPoolName());
        return adapter;
    }

    /**
     * 创建 Hikari 适配器
     */
    private ThirdPartyThreadPoolAdapter createHikariAdapter(
            ThirdPartyThreadPoolProperties.PoolConfig poolConfig) {

        try {
            DataSource dataSource = applicationContext.getBean(DataSource.class);
            HikariThreadPoolAdapter adapter = new HikariThreadPoolAdapter(poolConfig.getName(), dataSource);

            if (!adapter.isAvailable()) {
                log.warn("Hikari adapter is not available for [{}]", poolConfig.getName());
                return null;
            }

            log.info("Successfully created Hikari adapter [{}]", adapter.getPoolName());
            return adapter;
        } catch (Exception e) {
            log.warn("DataSource not found, cannot create Hikari adapter: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 注册适配器并应用初始配置
     */
    private void registerAdapter(
            ThirdPartyThreadPoolAdapter adapter,
            ThirdPartyThreadPoolProperties.PoolConfig poolConfig) {

        registry.registerThirdPartyAdapter(adapter);
        log.info("Successfully registered third-party thread pool [{}]", adapter.getPoolName());

        if (!hasInitialConfig(poolConfig)) {
            return;
        }

        applyInitialConfig(adapter, poolConfig);
    }

    /**
     * 应用初始配置
     */
    private void applyInitialConfig(
            ThirdPartyThreadPoolAdapter adapter,
            ThirdPartyThreadPoolProperties.PoolConfig poolConfig) {

        try {
            adapter.updateConfig(poolConfig.toConfig());
            log.info("Applied initial configuration to [{}]", adapter.getPoolName());
        } catch (Exception e) {
            log.warn("Failed to apply initial configuration to [{}]: {}",
                    adapter.getPoolName(), e.getMessage());
        }
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
