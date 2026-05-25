package io.github.archer099.threadpool.alarm;

import io.github.archer099.threadpool.actuator.ThreadPoolMetrics;
import io.github.archer099.threadpool.alarm.notifier.AlarmNotifier;
import io.github.archer099.threadpool.custom.config.DynamicThreadPoolProperties;
import io.github.archer099.threadpool.registry.ThreadPoolRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 线程池告警处理器
 * 定时检查线程池指标并触发告警
 *
 * @author archer099
 */
@Slf4j
public class ThreadPoolAlarmHandler {

    private final ThreadPoolRegistry registry;
    private final DynamicThreadPoolProperties properties;
    private final List<AlarmNotifier> alarmNotifiers;
    
    private final String applicationName;
    private final String serviceIp;

    /**
     * 记录上次告警时间（防止告警风暴）
     * Key: poolName-alarmType
     */
    private final Map<String, LocalDateTime> lastAlarmTime = new ConcurrentHashMap<>();

    public ThreadPoolAlarmHandler(ThreadPoolRegistry registry,
                                  DynamicThreadPoolProperties properties,
                                  List<AlarmNotifier> alarmNotifiers,
                                  @Value("${spring.application.name:unknown}") String applicationName) {
        this.registry = registry;
        this.properties = properties;
        this.alarmNotifiers = alarmNotifiers;
        this.applicationName = applicationName;
        this.serviceIp = getLocalIp();
        log.info("ThreadPoolAlarmHandler initialized with {} notifiers, app={}, ip={}", 
                alarmNotifiers.size(), applicationName, serviceIp);
    }
    
    /**
     * 获取本地IP地址
     */
    private String getLocalIp() {
        try {
            InetAddress address = InetAddress.getLocalHost();
            return address.getHostAddress();
        } catch (UnknownHostException e) {
            log.warn("Failed to get local IP address", e);
            return "unknown";
        }
    }

    /**
     * 定时检查告警
     */
    @Scheduled(fixedDelayString = "#{@dynamicThreadPoolProperties.alarm.alarmInterval.toMillis() / 2}"
    )
    public void checkAlarms() {
        if (!properties.getAlarm().getEnabled()) {
            return;
        }

        Map<String, ThreadPoolMetrics> metricsMap = registry.collectAllMetrics();
        metricsMap.forEach((poolName, metrics) -> {
            try {
                checkQueueUsage(metrics);
                checkActiveThreadRatio(metrics);
                checkRejectCount(metrics);
            } catch (Exception e) {
                log.error("Failed to check alarms for thread pool [{}]", poolName, e);
            }
        });
    }

    /**
     * 检查队列使用率
     */
    private void checkQueueUsage(ThreadPoolMetrics metrics) {
        Double threshold = properties.getAlarm().getQueueUsageThreshold();
        if (threshold == null || metrics.getQueueUsageRatio() < threshold) {
            return;
        }

        AlarmEvent event = AlarmEvent.builder()
                .applicationName(applicationName)
                .serviceIp(serviceIp)
                .poolName(metrics.getPoolName())
                .alarmType(AlarmEvent.AlarmType.QUEUE_USAGE_HIGH)
                .metricName("队列使用率")
                .currentValue(metrics.getQueueUsageRatio())
                .threshold(threshold)
                .message(String.format("线程池 [%s] 队列使用率 %.2f%% 超过阈值 %.2f%%",
                        metrics.getPoolName(),
                        metrics.getQueueUsageRatio() * 100,
                        threshold * 100))
                .alarmTime(LocalDateTime.now())
                .metrics(metrics)
                .build();

        triggerAlarm(event);
    }

    /**
     * 检查活跃线程比例
     */
    private void checkActiveThreadRatio(ThreadPoolMetrics metrics) {
        Double threshold = properties.getAlarm().getActiveThreadRatioThreshold();
        if (threshold == null || metrics.getActiveThreadRatio() < threshold) {
            return;
        }

        AlarmEvent event = AlarmEvent.builder()
                .applicationName(applicationName)
                .serviceIp(serviceIp)
                .poolName(metrics.getPoolName())
                .alarmType(AlarmEvent.AlarmType.ACTIVE_THREAD_RATIO_HIGH)
                .metricName("活跃线程比例")
                .currentValue(metrics.getActiveThreadRatio())
                .threshold(threshold)
                .message(String.format("线程池 [%s] 活跃线程比例 %.2f%% 超过阈值 %.2f%%",
                        metrics.getPoolName(),
                        metrics.getActiveThreadRatio() * 100,
                        threshold * 100))
                .alarmTime(LocalDateTime.now())
                .metrics(metrics)
                .build();

        triggerAlarm(event);
    }

    /**
     * 检查拒绝次数
     */
    private void checkRejectCount(ThreadPoolMetrics metrics) {
        Long threshold = properties.getAlarm().getRejectCountThreshold();
        if (threshold == null || metrics.getRejectCount() < threshold) {
            return;
        }

        AlarmEvent event = AlarmEvent.builder()
                .applicationName(applicationName)
                .serviceIp(serviceIp)
                .poolName(metrics.getPoolName())
                .alarmType(AlarmEvent.AlarmType.REJECT_COUNT_HIGH)
                .metricName("拒绝次数")
                .currentValue(metrics.getRejectCount().doubleValue())
                .threshold(threshold.doubleValue())
                .message(String.format("线程池 [%s] 拒绝次数 %d 超过阈值 %d",
                        metrics.getPoolName(),
                        metrics.getRejectCount(),
                        threshold))
                .alarmTime(LocalDateTime.now())
                .metrics(metrics)
                .build();

        triggerAlarm(event);
    }

    /**
     * 触发告警（带节流）
     */
    private void triggerAlarm(AlarmEvent event) {
        String key = event.getPoolName() + "-" + event.getAlarmType();
        LocalDateTime lastTime = lastAlarmTime.get(key);
        LocalDateTime now = LocalDateTime.now();

        // 检查是否在告警间隔内
        if (lastTime != null) {
            long intervalSeconds = properties.getAlarm().getAlarmInterval().getSeconds();
            if (lastTime.plusSeconds(intervalSeconds).isAfter(now)) {
                log.debug("Alarm for [{}] is throttled, last alarm time: {}", key, lastTime);
                return;
            }
        }

        lastAlarmTime.put(key, now);
        handleAlarm(event);
    }

    /**
     * 处理告警事件 - 使用策略模式委托给所有 AlarmNotifier
     */
    protected void handleAlarm(AlarmEvent event) {
        log.warn("ALARM triggered: {}", event.getMessage());

        // 委托给所有告警通知器
        for (AlarmNotifier notifier : alarmNotifiers) {
            try {
                notifier.sendAlarm(event);
            } catch (Exception e) {
                log.error("Failed to send alarm via notifier [{}]", notifier.getType(), e);
            }
        }
    }

    /**
     * 发送配置变更告警
     */
    public void sendConfigChangeAlarm(String poolName, String oldConfig, String newConfig) {
        if (!properties.getAlarm().getEnabled()) {
            return;
        }

        AlarmEvent event = AlarmEvent.builder()
                .applicationName(applicationName)
                .serviceIp(serviceIp)
                .poolName(poolName)
                .alarmType(AlarmEvent.AlarmType.CONFIG_CHANGED)
                .metricName("配置变更")
                .currentValue(null)
                .threshold(null)
                .message(String.format("线程池 [%s] 配置已变更\n变更前: %s\n变更后: %s",
                        poolName, oldConfig, newConfig))
                .alarmTime(LocalDateTime.now())
                .metrics(null)  // 配置变更时暂无实时指标
                .build();

        // 配置变更告警不节流，总是发送
        handleAlarm(event);
    }
}
