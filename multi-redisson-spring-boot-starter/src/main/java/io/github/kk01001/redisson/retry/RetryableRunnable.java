package io.github.archer099.redisson.retry;

/**
 * @author archer099
 * @date 2026-03-06 21:32:00
 * @description 可重试的 Runnable 包装，携带操作元信息用于拒绝时恢复
 */
public class RetryableRunnable implements Runnable {

    /**
     * 原始操作
     */
    private final Runnable action;

    /**
     * 操作名称
     */
    private final String actionName;

    public RetryableRunnable(Runnable action, String actionName) {
        this.action = action;
        this.actionName = actionName;
    }

    @Override
    public void run() {
        action.run();
    }

    public Runnable getAction() {
        return action;
    }

    public String getActionName() {
        return actionName;
    }
}
