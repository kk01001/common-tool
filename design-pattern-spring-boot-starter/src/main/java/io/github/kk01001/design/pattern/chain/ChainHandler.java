package io.github.kk01001.design.pattern.chain;

/**
 * @author linshiqiang
 * @date 2025-03-03 09:58:00
 * @description 责任链模式的核心接口，定义了处理器的执行方法和控制逻辑
 * @param <T> 处理的数据类型
 * @param <R> 返回的结果类型
 */
public interface ChainHandler<T, R> {
    /**
     * 处理请求
     * @param context 责任链上下文
     * @param data 待处理的数据
     * @return 处理结果
     */
    R handle(ChainContext<T, R> context, T data);
    
    /**
     * 获取处理器顺序
     * @return 顺序值，值越小优先级越高
     */
    default int getOrder() {
        return 0;
    }
    
    /**
     * 是否继续执行责任链
     * @param result 当前处理结果
     * @return true表示继续执行，false表示中断责任链
     */
    default boolean shouldContinue(R result) {
        return true;
    }
}