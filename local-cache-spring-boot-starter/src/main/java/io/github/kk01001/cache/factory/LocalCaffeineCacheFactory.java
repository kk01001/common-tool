package io.github.kk01001.cache.factory;

import io.github.kk01001.cache.core.AbstractLocalCaffeineCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author kk01001
 * @date 2024-03-24 14:31:00
 * @description 本地缓存工厂
 */
@Slf4j
@Component
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
     */
    public static String getCacheStats() {
        StringBuilder stats = new StringBuilder();
        CACHE_INSTANCES.forEach((clazz, cache) -> {
            stats.append(clazz.getSimpleName())
                    .append(": size=")
                    .append(cache.size())
                    .append("\n");
        });
        return stats.toString();
    }
} 