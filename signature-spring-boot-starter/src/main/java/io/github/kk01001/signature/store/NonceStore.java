package io.github.archer099.signature.store;

import java.time.Duration;

/**
 * @author archer099
 * @date 2026-01-18 13:23:23
 * @description Nonce 存储接口，用于防重放攻击，由用户实现具体存储逻辑
 */
public interface NonceStore {
    
    /**
     * 检查 nonce 是否存在（是否已使用）
     *
     * @param nonce 随机数
     * @return 是否存在
     */
    boolean exists(String nonce);
    
    /**
     * 存储 nonce（标记为已使用）
     *
     * @param nonce 随机数
     * @param ttl   过期时间
     * @return 是否存储成功（如果已存在返回 false）
     */
    boolean store(String nonce, Duration ttl);
    
    /**
     * 存储 nonce（标记为已使用），如果已存在则返回 false
     * 原子操作：检查并存储
     *
     * @param nonce 随机数
     * @param ttl   过期时间
     * @return 是否存储成功（如果已存在返回 false）
     */
    default boolean storeIfAbsent(String nonce, Duration ttl) {
        if (exists(nonce)) {
            return false;
        }
        return store(nonce, ttl);
    }
    
    /**
     * 删除 nonce
     *
     * @param nonce 随机数
     */
    default void remove(String nonce) {
        // 默认空实现
    }
}
