package io.github.kk01001.lock.model;

import io.github.kk01001.lock.enums.LockType;
import io.github.kk01001.lock.enums.RedisClientType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.TimeUnit;

/**
 * @author linshiqiang
 * @date 2024-09-06 22:09:00
 * @description
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Rule {

    /**
     * 是否开启锁
     */
    private Boolean enable = true;

    /**
     * 是否为阻塞锁
     */
    private Boolean block = true;

    /**
     * 锁类型
     */
    private LockType lockType = LockType.REDISSON_LOCK;

    /**
     * redis客户端类型
     */
    private RedisClientType redisClientType = RedisClientType.REDISSON;

    /**
     * 锁的key
     */
    private String key;

    /**
     * 总的凭证数
     */
    private Integer permits = 1;

    /**
     * true公平 false非公平
     */
    private Boolean fair = true;

    /**
     * 锁超时时间
     */
    private Long timeout = 1000L;

    /**
     * 单位
     */
    private TimeUnit timeUnit = TimeUnit.MILLISECONDS;
}
