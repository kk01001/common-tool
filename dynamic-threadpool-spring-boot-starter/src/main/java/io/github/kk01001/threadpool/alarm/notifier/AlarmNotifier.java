package io.github.archer099.threadpool.alarm.notifier;

import io.github.archer099.threadpool.alarm.AlarmEvent;

/**
 * 告警通知器接口
 *
 * @author archer099
 */
public interface AlarmNotifier {

    /**
     * 发送告警通知
     *
     * @param event 告警事件
     */
    void sendAlarm(AlarmEvent event);

    /**
     * 获取通知器类型
     */
    String getType();
}
