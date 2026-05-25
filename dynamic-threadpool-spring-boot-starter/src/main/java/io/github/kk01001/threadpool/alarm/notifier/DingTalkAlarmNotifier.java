package io.github.archer099.threadpool.alarm.notifier;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import io.github.archer099.threadpool.alarm.AlarmEvent;
import io.github.archer099.threadpool.custom.config.DynamicThreadPoolProperties;
import lombok.extern.slf4j.Slf4j;

/**
 * 钉钉告警通知器
 *
 * @author archer099
 */
@Slf4j
public class DingTalkAlarmNotifier implements AlarmNotifier {

    private final DynamicThreadPoolProperties.AlarmConfig alarmConfig;

    public DingTalkAlarmNotifier(DynamicThreadPoolProperties.AlarmConfig alarmConfig) {
        this.alarmConfig = alarmConfig;
    }

    @Override
    public void sendAlarm(AlarmEvent event) {
        if (alarmConfig.getDingTalkWebhook() == null || alarmConfig.getDingTalkWebhook().isEmpty()) {
            log.warn("DingTalk webhook is not configured, skipping DingTalk alarm");
            return;
        }

        try {
            String message = buildMarkdownMessage(event);
            JSONObject body = new JSONObject();
            body.set("msgtype", "markdown");

            JSONObject markdown = new JSONObject();
            markdown.set("title", "线程池告警");
            markdown.set("text", message);
            body.set("markdown", markdown);

            // @ 指定人员
            if (Boolean.TRUE.equals(alarmConfig.getMentionedAll()) ||
                    (alarmConfig.getMentionedMobileList() != null && !alarmConfig.getMentionedMobileList().isEmpty())) {
                JSONObject at = new JSONObject();
                at.set("isAtAll", Boolean.TRUE.equals(alarmConfig.getMentionedAll()));
                if (alarmConfig.getMentionedMobileList() != null) {
                    at.set("atMobiles", alarmConfig.getMentionedMobileList());
                }
                body.set("at", at);
            }

            String response = HttpRequest.post(alarmConfig.getDingTalkWebhook())
                    .header("Content-Type", "application/json")
                    .body(JSONUtil.toJsonStr(body))
                    .timeout(5000)
                    .execute()
                    .body();

            log.info("DingTalk alarm sent successfully for pool [{}], response: {}", event.getPoolName(), response);
        } catch (Exception e) {
            log.error("Failed to send DingTalk alarm for pool [{}]", event.getPoolName(), e);
        }
    }

    @Override
    public String getType() {
        return "dingtalk";
    }

    /**
     * 构建 Markdown 格式消息
     */
    private String buildMarkdownMessage(AlarmEvent event) {
        StringBuilder sb = new StringBuilder();
        sb.append("## 🚨 线程池告警\n\n");
        
        // 应用信息
        sb.append("**应用名称**: ").append(event.getApplicationName()).append("\n\n");
        sb.append("**服务IP**: ").append(event.getServiceIp()).append("\n\n");
        sb.append("**线程池名称**: ").append(event.getPoolName()).append("\n\n");
        
        // 告警信息
        sb.append("**告警类型**: ").append(getAlarmTypeName(event.getAlarmType())).append("\n\n");
        sb.append("**告警指标**: ").append(event.getMetricName()).append("\n\n");
        if (event.getCurrentValue() != null) {
            sb.append("**当前值**: ").append(formatValue(event.getCurrentValue())).append("\n\n");
        }
        if (event.getThreshold() != null) {
            sb.append("**阈值**: ").append(formatValue(event.getThreshold())).append("\n\n");
        }
        sb.append("**告警时间**: ").append(event.getAlarmTime()).append("\n\n");
        
        // 详细信息
        sb.append("---\n\n");
        sb.append("**详细信息**: ").append(event.getMessage()).append("\n\n");
        
        // 如果有完整指标，显示其他相关信息
        if (event.getMetrics() != null) {
            sb.append("### 📊 线程池当前状态\n\n");
            var m = event.getMetrics();
            sb.append("- 核心线程数: ").append(m.getCorePoolSize()).append("\n");
            sb.append("- 最大线程数: ").append(m.getMaxPoolSize()).append("\n");
            sb.append("- 当前线程数: ").append(m.getPoolSize()).append("\n");
            sb.append("- 活跃线程数: ").append(m.getActiveThreadCount()).append("\n");
            sb.append("- 队列容量: ").append(m.getQueueCapacity()).append("\n");
            sb.append("- 队列大小: ").append(m.getQueueSize()).append("\n");
            sb.append("- 队列使用率: ").append(String.format("%.2f%%", m.getQueueUsageRatio() * 100)).append("\n");
            sb.append("- 活跃线程比例: ").append(String.format("%.2f%%", m.getActiveThreadRatio() * 100)).append("\n");
            sb.append("- 已完成任务数: ").append(m.getCompletedTaskCount()).append("\n");
            sb.append("- 拒绝次数: ").append(m.getRejectCount()).append("\n");
        }

        return sb.toString();
    }

    private String getAlarmTypeName(AlarmEvent.AlarmType type) {
        return switch (type) {
            case QUEUE_USAGE_HIGH -> "队列使用率过高";
            case ACTIVE_THREAD_RATIO_HIGH -> "活跃线程比例过高";
            case REJECT_COUNT_HIGH -> "拒绝次数过多";
            case CONFIG_CHANGED -> "配置变更";
        };
    }

    private String formatValue(Double value) {
        if (value == null) {
            return "N/A";
        }
        if (value < 1.0) {
            return String.format("%.2f%%", value * 100);
        }
        return String.format("%.0f", value);
    }
}
