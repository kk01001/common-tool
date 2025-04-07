package io.github.kk01001.design.pattern.statemachine.persister;

/**
 * @author kk01001
 * @date 2024-04-07 14:31:00
 * @description 状态持久化接口
 */
public interface StatePersister<S, C> {

    /**
     * 写入状态
     *
     * @param context 上下文
     * @param state   状态
     */
    void write(C context, S state);

    /**
     * 读取状态
     *
     * @param context 上下文
     * @return 状态
     */
    S read(C context);
} 