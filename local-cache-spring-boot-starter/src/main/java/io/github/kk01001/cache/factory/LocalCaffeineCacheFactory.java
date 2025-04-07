package io.github.kk01001.cache.factory;

import com.github.benmanes.caffeine.cache.stats.CacheStats;
import io.github.kk01001.cache.core.AbstractLocalCaffeineCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author kk01001
 * @date 2024-03-24 14:31:00
 * @description 本地缓存工厂
 */
@Slf4j
public class LocalCaffeineCacheFactory implements BeanFactoryPostProcessor, ApplicationContextAware {

    private static ApplicationContext applicationContext;
    /**
     * 缓存实例容器
     */
    private static final Map<Class<?>, AbstractLocalCaffeineCache<?, ?>> CACHE_INSTANCES = new ConcurrentHashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        LocalCaffeineCacheFactory.applicationContext = applicationContext;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // 获取所有AbstractLocalCache的子类
        Map<String, AbstractLocalCaffeineCache> caches = applicationContext.getBeansOfType(AbstractLocalCaffeineCache.class);

        // 注册所有缓存实例
        caches.forEach((beanName, cache) -> {
            Class<?> cacheClass = cache.getClass();
            CACHE_INSTANCES.put(cacheClass, cache);
            log.info("Registered local cache: {}", cacheClass.getSimpleName());
        });
    }

    /**
     * 获取缓存实例
     */
    @SuppressWarnings("unchecked")
    public static <T extends AbstractLocalCaffeineCache<?, ?>> T getCache(Class<T> cacheClass) {
        return (T) CACHE_INSTANCES.get(cacheClass);
    }

    /**
     * 获取所有缓存实例
     */
    public static Map<Class<?>, AbstractLocalCaffeineCache<?, ?>> getAllCaches() {
        return CACHE_INSTANCES;
    }

    /**
     * 清空所有缓存
     */
    public static void clearAllCaches() {
        CACHE_INSTANCES.values().forEach(AbstractLocalCaffeineCache::clear);
    }

    /**
     * 获取缓存统计信息
     *
     * @return 返回包含各缓存实例统计信息的Map
     */
    public static Map<String, Map<String, Object>> getCacheStats() {
        Map<String, Map<String, Object>> statsMap = new HashMap<>();

        CACHE_INSTANCES.forEach((clazz, cache) -> {
            Map<String, Object> cacheStats = new HashMap<>();
            // 基本信息
            cacheStats.put("size", cache.size());
            // 获取命中率等信息
            CacheStats stats = cache.stats();
            if (stats != null) {
                cacheStats.put("hitCount", stats.hitCount());
                cacheStats.put("missCount", stats.missCount());
                cacheStats.put("hitRate", String.format("%.2f%%", stats.hitRate() * 100));
                cacheStats.put("missRate", String.format("%.2f%%", stats.missRate() * 100));
                cacheStats.put("evictionCount", stats.evictionCount());
                cacheStats.put("loadSuccessCount", stats.loadSuccessCount());
                cacheStats.put("loadFailureCount", stats.loadFailureCount());
                cacheStats.put("totalLoadTime", stats.totalLoadTime());
                cacheStats.put("averageLoadPenalty", stats.averageLoadPenalty());
                cacheStats.put("loadFailureRate", String.format("%.2f%%", stats.loadFailureRate() * 100));
                cacheStats.put("requestCount", stats.requestCount());
                cacheStats.put("evictionWeight", stats.evictionWeight());
            }

            statsMap.put(clazz.getSimpleName(), cacheStats);
        });

        return statsMap;
    }
} 