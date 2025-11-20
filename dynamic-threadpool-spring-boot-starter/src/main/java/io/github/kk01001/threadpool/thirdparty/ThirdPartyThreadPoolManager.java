package io.github.kk01001.threadpool.thirdparty;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 第三方线程池管理器 (单例模式)
 * 
 * <p>负责管理所有第三方线程池适配器的生命周期
 * 
 * @author kk01001
 */
@Slf4j
public class ThirdPartyThreadPoolManager {
    
    private final ThirdPartyThreadPoolAdapterFactory adapterFactory;
    private final Map<String, ThirdPartyThreadPoolAdapter> adapters = new ConcurrentHashMap<>();
    
    public ThirdPartyThreadPoolManager(ApplicationContext applicationContext) {
        this.adapterFactory = new ThirdPartyThreadPoolAdapterFactory(applicationContext);
    }
    
    /**
     * 注册第三方线程池适配器
     * 
     * @param poolType 线程池类型
     * @param poolName 线程池名称（不包含类型前缀）
     * @return 是否注册成功
     */
    public boolean registerAdapter(ThirdPartyPoolType poolType, String poolName) {
        try {
            ThirdPartyThreadPoolAdapter adapter = adapterFactory.createAdapter(poolType, poolName);
            
            if (adapter == null) {
                log.warn("Failed to create adapter for type [{}], name [{}]", poolType, poolName);
                return false;
            }
            
            String fullName = adapter.getPoolName();
            adapters.put(fullName, adapter);
            log.info("Successfully registered third-party thread pool adapter [{}]", fullName);
            return true;
        } catch (Exception e) {
            log.error("Failed to register adapter for type [{}], name [{}]", poolType, poolName, e);
            return false;
        }
    }
    
    /**
     * 获取适配器
     */
    public ThirdPartyThreadPoolAdapter getAdapter(String poolName) {
        return adapters.get(poolName);
    }
    
    /**
     * 获取所有适配器
     */
    public Map<String, ThirdPartyThreadPoolAdapter> getAllAdapters() {
        return new ConcurrentHashMap<>(adapters);
    }
    
    /**
     * 移除适配器
     */
    public void removeAdapter(String poolName) {
        ThirdPartyThreadPoolAdapter removed = adapters.remove(poolName);
        if (removed != null) {
            log.info("Removed third-party thread pool adapter [{}]", poolName);
        }
    }
    
    /**
     * 检查适配器是否存在
     */
    public boolean hasAdapter(String poolName) {
        return adapters.containsKey(poolName);
    }
    
    /**
     * 获取适配器数量
     */
    public int getAdapterCount() {
        return adapters.size();
    }
    
    /**
     * 检查指定类型的适配器是否支持
     */
    public boolean isSupported(ThirdPartyPoolType poolType) {
        return adapterFactory.isSupported(poolType);
    }
}
