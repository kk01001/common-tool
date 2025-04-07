package io.github.kk01001.design.pattern.statemachine.persister;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author kk01001
 * @date 2024-04-07 14:31:00
 * @description 本地状态持久化实现
 */
public class InMemoryStatePersister<S, C> implements StatePersister<S, C> {

    /**
     * 状态存储
     */
    private final ConcurrentHashMap<String, S> stateMap = new ConcurrentHashMap<>();

    @Override
    public void write(C context, S state) {
        if (context != null && state != null) {
            stateMap.put(context.toString(), state);
        }
    }

    @Override
    public S read(C context) {
        return context != null ? stateMap.get(context.toString()) : null;
    }

    /**
     * 清除状态
     *
     * @param context 上下文
     */
    public void remove(C context) {
        if (context != null) {
            stateMap.remove(context.toString());
        }
    }

    /**
     * 清除所有状态
     */
    public void clear() {
        stateMap.clear();
    }
} 