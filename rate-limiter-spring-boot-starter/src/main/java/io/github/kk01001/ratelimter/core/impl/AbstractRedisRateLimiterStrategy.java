package io.github.kk01001.ratelimter.core.impl;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.extra.spring.SpringUtil;
import io.github.kk01001.ratelimter.enums.RedisClientType;
import io.github.kk01001.ratelimter.model.Rule;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Objects;

/**
 * @author linshiqiang
 * @date 2024-09-08 14:50:00
 * @description
 */
public abstract class AbstractRedisRateLimiterStrategy {

    public boolean tryAccess(Rule rule, Object... values) {
        RedisClientType redisClientType = rule.getRedisClientType();
        if (RedisClientType.REDISSON.equals(redisClientType)) {
            return getResult(executeRedisson(ListUtil.of(rule.getKey()), values));
        }
        return getResult(executeRedisTemplate(ListUtil.of(rule.getKey()), values));
    }

    private Object executeRedisson(List<Object> keys, Object... values) {
        RedissonClient redissonClient = getRedissonClient();
        Assert.notNull(redissonClient, "Bean[redissonClient] is not exist");

        RScript script = redissonClient.getScript();
        return script.eval(RScript.Mode.READ_WRITE,
                getScript(),
                RScript.ReturnType.VALUE,
                keys,
                values);
    }

    private Object executeRedisTemplate(List<String> keys, Object... values) {
        RedisTemplate<String, Object> redisTemplate = getRedisTemplate();
        Assert.notNull(redisTemplate, "Bean[redisTemplate] is not exist");

        DefaultRedisScript<Object> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(getScript());
        redisScript.setResultType(Object.class);
        return redisTemplate.execute(redisScript, keys, values);
    }

    private boolean getResult(Object result) {
        return Objects.nonNull(result) && 1 == (long) result;
    }

    private RedissonClient getRedissonClient() {
        return SpringUtil.getBean(RedissonClient.class);
    }

    private RedisTemplate<String, Object> getRedisTemplate() {
        return SpringUtil.getBean(new TypeReference<RedisTemplate<String, Object>>() {
        });
    }

    protected abstract String getScript();

}
