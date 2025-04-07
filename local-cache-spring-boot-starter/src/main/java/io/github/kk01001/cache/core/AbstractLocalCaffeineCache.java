package io.github.kk01001.cache.core;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * @author kk01001
 * @date 2024-03-24 14:31:00
 * @description 抽象本地缓存类
 */
@Slf4j
public abstract class AbstractLocalCaffeineCache<K, V> {

    /**
     * Caffeine缓存实例
     */
    private final Cache<K, V> cache = createCache();

    /**
     * 创建缓存实例
     */
    protected Cache<K, V> createCache() {
        return Caffeine.newBuilder()
                .expireAfterAccess(getExpireAfterAccess())
                .maximumSize(getMaximumSize())
                .initialCapacity(getInitialCapacity())
                .removalListener(this::onRemoval)
                .build();
    }

    /**
     * 获取缓存过期时间（分钟）
     *
     * @return 过期时间
     */
    protected abstract Duration getExpireAfterAccess();

    /**
     * 获取最大缓存条数
     *
     * @return 最大缓存条数
     */
    protected abstract long getMaximumSize();

    /**
     * 获取初始容量
     *
     * @return 初始容量
     */
    protected abstract int getInitialCapacity();

    /**
     * 缓存移除监听器
     *
     * @param key   键
     * @param value 值
     * @param cause 移除原因
     */
    protected abstract void onRemoval(K key, V value, RemovalCause cause);

    /**
     * 获取缓存值
     */
    public V get(K key) {
        return cache.getIfPresent(key);
    }

    /**
     * 获取缓存值，如果不存在则通过supplier获取并缓存
     */
    public V get(K key, Supplier<V> supplier) {
        return cache.get(key, k -> supplier.get());
    }

    /**
     * 放入缓存
     */
    public void put(K key, V value) {
        cache.put(key, value);
    }

    /**
     * 删除缓存
     */
    public void remove(K key) {
        cache.invalidate(key);
    }

    /**
     * 清空缓存
     */
    public void clear() {
        cache.invalidateAll();
    }

    /**
     * 获取缓存大小
     */
    public long size() {
        return cache.estimatedSize();
    }

    public CacheStats stats() {
        return cache.stats();
    }
} 