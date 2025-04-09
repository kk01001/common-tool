package io.github.kk01001.design.pattern.statemachine.persister;

import io.github.kk01001.design.pattern.statemachine.config.StateMachineProperties;
import io.github.kk01001.design.pattern.statemachine.exception.StatePersistException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.util.Assert;

/**
 * @author kk01001
 * @date 2024-04-10 16:00:00
 * @description 使用Redisson进行状态持久化的实现
 */
@Slf4j
@RequiredArgsConstructor
public class RedissonStatePersister<S, C> implements StatePersister<S, C> {

    private final RedissonClient redissonClient;
    private final StateMachineProperties stateMachineProperties;

    /**
     * 生成存储状态的Redis Key 默认使用 状态机名称 + 状态机ID
     *
     * @param machineName 状态机名称
     * @param machineId   状态机ID
     * @param context     上下文 (可选，用于更精细的Key生成)
     * @return Redis Key
     */
    protected String getKey(String machineName, String machineId, C context) {
        String keyPrefix = stateMachineProperties.getPersister().getKeyPrefix();
        return String.format("%s:%s:%s", keyPrefix, machineName, machineId);
    }

    /**
     * 生成存储具体状态机实例状态的Map Field Key
     *
     * @param machineId 状态机ID
     * @param context   上下文 (可选)
     * @return RMap中的Field Key
     */
    protected String getMapFieldKey(String machineId, C context) {
        // 默认只使用machineId作为field key
        return machineId;
    }

    @Override
    public void write(String machineName, String machineId, C context, S state) {
        Assert.hasText(machineName, "MachineName must not be empty");
        Assert.hasText(machineId, "MachineId must not be empty");
        Assert.notNull(state, "State must not be null");
        // Context 可以为 null

        String key = getKey(machineName, machineId, context);

        try {
            redissonClient.getBucket(key).set(state, stateMachineProperties.getPersister().getTimeout());
            log.debug("Redisson Persisted state for machine [{}], id [{}]: {}", machineName, machineId, state);
        } catch (Exception e) {
            log.error("Error writing state to Redisson for machine [{}], id [{}]: {}", machineName, machineId, state, e);
            throw new StatePersistException("Failed to persist state to Redisson", e);
        }
    }

    @Override
    public S read(String machineName, String machineId, C context) {
        Assert.hasText(machineName, "MachineName must not be empty");
        Assert.hasText(machineId, "MachineId must not be empty");
        // Context 可以为 null

        String key = getKey(machineName, machineId, context);

        try {
            RBucket<S> bucket = redissonClient.getBucket(key);
            S state = bucket.get();
            log.debug("Redisson Read state for machine [{}], id [{}]: {}", machineName, machineId, state);
            return state;
        } catch (Exception e) {
            log.error("Error reading state from Redisson for machine [{}], id [{}]", machineName, machineId, e);
        }
        return null;
    }

    @Override
    public void remove(String machineName, String machineId, C context) {
        Assert.hasText(machineName, "MachineName must not be empty");
        Assert.hasText(machineId, "MachineId must not be empty");
        // Context 可以为 null

        String key = getKey(machineName, machineId, context);

        try {
            RBucket<S> bucket = redissonClient.getBucket(key);
            bucket.delete();
            log.debug("Redisson State not found or already removed for machine [{}], id [{}]", machineName, machineId);
        } catch (Exception e) {
            log.error("Error removing state from Redisson for machine [{}], id [{}]", machineName, machineId, e);
        }
    }
}
