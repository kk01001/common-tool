package io.github.kk01001.threadpool.alarm.notifier;

import io.github.kk01001.threadpool.model.AlarmEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * 日志告警通知器
 *
 * @author kk01001
 */
@Slf4j
public class LogAlarmNotifier implements AlarmNotifier {

    @Override
    public void sendAlarm(AlarmEvent event) {
        log.warn("========== THREAD POOL ALARM ==========");
        log.warn("Application: {}", event.getApplicationName());
        log.warn("Service IP: {}", event.getServiceIp());
        log.warn("Pool Name: {}", event.getPoolName());
        log.warn("Alarm Type: {}", event.getAlarmType());
        log.warn("Metric: {}", event.getMetricName());
        if (event.getCurrentValue() != null) {
            log.warn("Current Value: {}", event.getCurrentValue());
        }
        if (event.getThreshold() != null) {
            log.warn("Threshold: {}", event.getThreshold());
        }
        log.warn("Message: {}", event.getMessage());
        log.warn("Time: {}", event.getAlarmTime());
        
        // 如果有完整指标，显示线程池当前状态
        if (event.getMetrics() != null) {
            var m = event.getMetrics();
            log.warn("---------- Thread Pool Status ----------");
            log.warn("Core Pool Size: {}", m.getCorePoolSize());
            log.warn("Max Pool Size: {}", m.getMaxPoolSize());
            log.warn("Current Pool Size: {}", m.getPoolSize());
            log.warn("Active Threads: {}", m.getActiveThreadCount());
            log.warn("Queue Capacity: {}", m.getQueueCapacity());
            log.warn("Queue Size: {}", m.getQueueSize());
            log.warn("Queue Usage: {:.2f}%", m.getQueueUsageRatio() * 100);
            log.warn("Active Thread Ratio: {:.2f}%", m.getActiveThreadRatio() * 100);
            log.warn("Completed Tasks: {}", m.getCompletedTaskCount());
            log.warn("Reject Count: {}", m.getRejectCount());
        }
        log.warn("========================================");
    }

    @Override
    public String getType() {
        return "log";
    }
}
