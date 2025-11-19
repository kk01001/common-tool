package io.github.kk01001.threadpool.alarm.notifier;

import io.github.kk01001.threadpool.model.AlarmEvent;

/**
 * 告警通知器接口
 *
 * @author kk01001
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
