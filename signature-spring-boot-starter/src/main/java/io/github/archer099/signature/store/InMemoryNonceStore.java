package io.github.archer099.signature.store;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * @author archer099
 * @date 2026-01-18 13:23:23
 * @description 基于内存的 Nonce 存储实现（仅适用于单机部署）
 */
@Slf4j
public class InMemoryNonceStore implements NonceStore {
    
    /**
     * 内存缓存
     */
    private final Cache<String, Boolean> cache;
    
    public InMemoryNonceStore(Duration defaultTtl) {
        this.cache = CacheBuilder.newBuilder()
                .expireAfterWrite(defaultTtl.toMillis(), TimeUnit.MILLISECONDS)
                .maximumSize(100000)
                .build();
        log.info("初始化内存 NonceStore，默认过期时间：{}秒", defaultTtl.toSeconds());
    }
    
    @Override
    public boolean exists(String nonce) {
        return cache.getIfPresent(nonce) != null;
    }
    
    @Override
    public boolean store(String nonce, Duration ttl) {
        cache.put(nonce, Boolean.TRUE);
        return true;
    }
    
    @Override
    public boolean storeIfAbsent(String nonce, Duration ttl) {
        synchronized (this) {
            if (exists(nonce)) {
                return false;
            }
            cache.put(nonce, Boolean.TRUE);
            return true;
        }
    }
    
    @Override
    public void remove(String nonce) {
        cache.invalidate(nonce);
    }
}
