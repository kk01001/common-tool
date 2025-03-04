package io.github.kk01001.design.pattern.responsibility;

/**
 * @param <T> 处理的数据类型
 * @param <R> 返回的结果类型
 * @author linshiqiang
 * @date 2025-03-03 09:58:00
 * @description 责任链模式的核心接口，定义了处理器的执行方法和控制逻辑
 */
public interface ResponsibilityChainHandler<T, R> {

    /**
     * 处理请求
     *
     * @param context 责任链上下文
     * @return 处理结果
     * @throws Exception 处理过程中可能抛出的异常
     */
    default R handle(ResponsibilityChainContext<T, R> context) throws Exception {
        return null;
    }

    /**
     * 处理请求
     *
     * @param context 责任链上下文
     * @throws Exception 处理过程中可能抛出的异常
     */
    default void handleVoid(ResponsibilityChainContext<T, R> context) throws Exception {
        
    }

    /**
     * 异常处理方法
     *
     * @param context 责任链上下文
     * @param e       异常信息
     * @return 处理结果
     */
    default R onError(ResponsibilityChainContext<T, R> context, Exception e) {
        throw new RuntimeException("Chain handler execution error", e);
    }

    /**
     * 获取处理器顺序
     *
     * @return 顺序值，值越小优先级越高
     */
    default int getOrder() {
        return 0;
    }

    /**
     * 是否继续执行责任链
     *
     * @param context 责任链上下文
     * @return true表示继续执行，false表示中断责任链
     */
    default boolean shouldTerminated(ResponsibilityChainContext<T, R> context) {
        return context.isTerminated();
    }
}