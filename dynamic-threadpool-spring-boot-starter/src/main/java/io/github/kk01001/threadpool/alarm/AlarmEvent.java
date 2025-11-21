package io.github.kk01001.threadpool.alarm;

import io.github.kk01001.threadpool.actuator.ThreadPoolMetrics;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 告警事件
 *
 * @author kk01001
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlarmEvent {

    /**
     * 应用名称
     */
    private String applicationName;

    /**
     * 服务IP地址
     */
    private String serviceIp;

    /**
     * 线程池名称
     */
    private String poolName;

    /**
     * 告警类型
     */
    private AlarmType alarmType;

    /**
     * 告警指标名称
     */
    private String metricName;

    /**
     * 阈值
     */
    private Double threshold;

    /**
     * 当前值
     */
    private Double currentValue;

    /**
     * 告警消息
     */
    private String message;

    /**
     * 告警时间
     */
    private LocalDateTime alarmTime;

    /**
     * 完整的线程池指标（用于显示上下文信息）
     */
    private ThreadPoolMetrics metrics;

    /**
     * 告警类型
     */
    public enum AlarmType {
        /**
         * 队列使用率过高
         */
        QUEUE_USAGE_HIGH,

        /**
         * 活跃线程比例过高
         */
        ACTIVE_THREAD_RATIO_HIGH,

        /**
         * 拒绝次数过多
         */
        REJECT_COUNT_HIGH,

        /**
         * 配置变更
         */
        CONFIG_CHANGED
    }
}
