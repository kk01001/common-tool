package io.github.kk01001.threadpool.thirdparty;

import io.github.kk01001.threadpool.thirdparty.adapter.TomcatThreadPoolAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

/**
 * 第三方线程池适配器工厂 (工厂模式)
 * 
 * <p>根据类型创建相应的适配器实例
 * 
 * @author kk01001
 */
@Slf4j
public class ThirdPartyThreadPoolAdapterFactory {
    
    private final ApplicationContext applicationContext;
    
    public ThirdPartyThreadPoolAdapterFactory(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
    
    /**
     * 创建适配器
     * 
     * @param poolType 线程池类型
     * @param poolName 线程池名称
     * @return 适配器实例，如果类型不支持则返回 null
     */
    public ThirdPartyThreadPoolAdapter createAdapter(ThirdPartyPoolType poolType, String poolName) {
        if (poolType == null || poolName == null) {
            throw new IllegalArgumentException("poolType and poolName cannot be null");
        }
        
        return switch (poolType) {
            case TOMCAT -> createTomcatAdapter(poolName);
            case UNDERTOW -> {
                log.warn("Undertow adapter not implemented yet");
                yield null;
            }
            case JETTY -> {
                log.warn("Jetty adapter not implemented yet");
                yield null;
            }
            case DUBBO -> {
                log.warn("Dubbo adapter not implemented yet");
                yield null;
            }
            case HIKARI -> {
                log.warn("Hikari adapter not implemented yet");
                yield null;
            }
            case GRPC -> {
                log.warn("gRPC adapter not implemented yet");
                yield null;
            }
        };
    }
    
    /**
     * 创建 Tomcat 适配器
     * @deprecated 直接在 Initializer 中创建，传入 WebServer
     */
    @Deprecated
    private ThirdPartyThreadPoolAdapter createTomcatAdapter(String poolName) {
        log.warn("createTomcatAdapter is deprecated, create adapter directly in Initializer");
        return null;
    }
    
    /**
     * 检查指定类型的适配器是否支持
     */
    public boolean isSupported(ThirdPartyPoolType poolType) {
        return switch (poolType) {
            case TOMCAT -> isTomcatAvailable();
            default -> false;
        };
    }
    
    /**
     * 检查 Tomcat 是否可用
     */
    private boolean isTomcatAvailable() {
        try {
            Class.forName("org.apache.catalina.connector.Connector");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
