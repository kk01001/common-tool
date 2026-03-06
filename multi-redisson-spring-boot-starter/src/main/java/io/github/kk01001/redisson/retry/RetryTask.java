package io.github.kk01001.redisson.retry;

import java.io.Serializable;

/**
 * @author kk01001
 * @date 2026-03-06 21:32:00
 * @description 双写重试任务封装
 */
public class RetryTask implements Serializable {

    /**
     * 要执行的操作
     */
    private final transient Runnable action;

    /**
     * 操作名称
     */
    private final String actionName;

    /**
     * 创建时间戳
     */
    private final long createTime;

    /**
     * 已重试次数
     */
    private int retryCount;

    /**
     * 下次重试时间戳
     */
    private long nextRetryTime;

    public RetryTask(Runnable action, String actionName) {
        this.action = action;
        this.actionName = actionName;
        this.createTime = System.currentTimeMillis();
        this.retryCount = 0;
        this.nextRetryTime = System.currentTimeMillis();
    }

    public Runnable getAction() {
        return action;
    }

    public String getActionName() {
        return actionName;
    }

    public long getCreateTime() {
        return createTime;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void incrementRetryCount() {
        this.retryCount++;
    }

    public long getNextRetryTime() {
        return nextRetryTime;
    }

    public void setNextRetryTime(long nextRetryTime) {
        this.nextRetryTime = nextRetryTime;
    }

    @Override
    public String toString() {
        return "RetryTask{" +
                "actionName='" + actionName + '\'' +
                ", createTime=" + createTime +
                ", retryCount=" + retryCount +
                ", nextRetryTime=" + nextRetryTime +
                '}';
    }
}
