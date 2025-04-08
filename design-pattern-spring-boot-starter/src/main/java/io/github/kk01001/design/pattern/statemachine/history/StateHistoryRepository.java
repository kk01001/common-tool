package io.github.kk01001.design.pattern.statemachine.history;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author kk01001
 * @date 2024-04-07 14:31:00
 * @description 状态历史记录存储库接口
 */
public interface StateHistoryRepository {
    
    /**
     * 保存状态转换历史记录
     *
     * @param history 状态转换历史记录
     */
    <S, E> void save(StateTransitionHistory<S, E> history);
    
    /**
     * 根据状态机名称和ID获取历史记录
     *
     * @param machineName 状态机名称
     * @param machineId 状态机ID
     * @return 历史记录列表
     */
    <S, E> List<StateTransitionHistory<S, E>> findByMachineNameAndMachineId(String machineName, String machineId);
    
    /**
     * 根据上下文ID获取历史记录
     *
     * @param contextId 上下文ID
     * @return 历史记录列表
     */
    <S, E> List<StateTransitionHistory<S, E>> findByContextId(String contextId);
    
    /**
     * 根据状态机名称、状态机ID和上下文ID获取历史记录
     *
     * @param machineName 状态机名称
     * @param machineId 状态机ID
     * @param contextId 上下文ID
     * @return 历史记录列表
     */
    <S, E> List<StateTransitionHistory<S, E>> findByMachineNameAndMachineIdAndContextId(
            String machineName, String machineId, String contextId);
    
    /**
     * 根据时间范围查询历史记录
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 历史记录列表
     */
    <S, E> List<StateTransitionHistory<S, E>> findByTransitionTimeBetween(
            LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 获取某个状态机实例在特定时间点的状态
     *
     * @param machineName 状态机名称
     * @param machineId 状态机ID
     * @param contextId 上下文ID
     * @param timestamp 时间点
     * @return 该时间点的状态，如果找不到则返回null
     */
    <S, E> S getStateAtTime(String machineName, String machineId, String contextId, LocalDateTime timestamp);
    
    /**
     * 清空所有历史记录
     */
    void clear();
} 