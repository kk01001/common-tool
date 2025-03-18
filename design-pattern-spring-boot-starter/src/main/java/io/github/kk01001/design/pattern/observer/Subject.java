package io.github.kk01001.design.pattern.observer;

import java.util.EventObject;

/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description 主题接口，所有消息实体都需要实现此接口以标识其所属主题
 */
public abstract class Subject extends EventObject {

    private final long timestamp;

    public Subject(Object source) {
        super(source);
        this.timestamp = System.currentTimeMillis();
    }

    public final long getTimestamp() {
        return this.timestamp;
    }

    /**
     * 获取主题名称
     *
     * @return 主题名称
     */
    public abstract String getTopic();
} 