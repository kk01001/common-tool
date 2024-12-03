package io.github.kk01001.lock.core.impl;

import cn.hutool.core.collection.ListUtil;
import io.github.kk01001.lock.core.LockStrategy;
import io.github.kk01001.lock.enums.LockType;
import io.github.kk01001.lock.manager.LuaScriptManager;
import io.github.kk01001.lock.model.LockRule;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author linshiqiang
 * @date 2024-09-17 20:14:00
 * @description redis template lua 信号量
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnBean(RedisTemplate.class)
public class RedisTemplateSemaphoreStrategyImpl implements LockStrategy {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public LockType getType() {
        return LockType.REDIS_TEMPLATE_SEMAPHORE;
    }

    @Override
    public void lock(LockRule lockRule) {
        tryAcquireDirectly(lockRule);
    }

    @SneakyThrows
    @Override
    public boolean tryLock(LockRule lockRule) {
        long time = lockRule.getTimeout();
        long current = System.currentTimeMillis();

        // 第一次尝试获取信号量
        if (tryAcquireDirectly(lockRule)) {
            return true;
        }

        // 减去第一次尝试获取所消耗的时间
        time -= System.currentTimeMillis() - current;
        if (time <= 0) {
            return false;
        }

        // 尝试订阅，模拟等待信号量释放
        while (true) {
            current = System.currentTimeMillis();
            if (tryAcquireDirectly(lockRule)) {
                return true;
            }

            // 再次检查剩余等待时间
            time -= System.currentTimeMillis() - current;
            if (time <= 0) {
                return false;
            }

            // 模拟等待信号量释放
            TimeUnit.MILLISECONDS.sleep(Math.min(100, time));

            time -= System.currentTimeMillis() - current;
            if (time <= 0) {
                return false;
            }
        }
    }

    @Override
    public void unlock(LockRule lockRule) {
        release(lockRule);
    }

    private boolean tryAcquireDirectly(LockRule lockRule) {
        String luaScript = LuaScriptManager.getSemaphoreTryAcquire();
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(luaScript);
        redisScript.setResultType(Long.class);
        Long result = redisTemplate.execute(redisScript, ListUtil.of(lockRule.getKey()), 1, lockRule.getPermits());
        return result != null && result == 1;
    }

    public void release(LockRule lockRule) {
        String luaScript = LuaScriptManager.getSemaphoreRelease();
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(luaScript);
        redisScript.setResultType(Long.class);
        redisTemplate.execute(redisScript, ListUtil.of(lockRule.getKey()), 1);
    }
}
