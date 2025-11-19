package io.github.kk01001.threadpool.alarm.notifier;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import io.github.kk01001.threadpool.config.DynamicThreadPoolProperties;
import io.github.kk01001.threadpool.model.AlarmEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 企业微信告警通知器
 *
 * @author kk01001
 */
@Slf4j
public class WeChatAlarmNotifier implements AlarmNotifier {

    private final DynamicThreadPoolProperties.AlarmConfig alarmConfig;

    public WeChatAlarmNotifier(DynamicThreadPoolProperties.AlarmConfig alarmConfig) {
        this.alarmConfig = alarmConfig;
    }

    @Override
    public void sendAlarm(AlarmEvent event) {
        if (alarmConfig.getWechatWebhook() == null || alarmConfig.getWechatWebhook().isEmpty()) {
            log.warn("WeChat webhook is not configured, skipping WeChat alarm");
            return;
        }

        try {
            String message = buildMarkdownMessage(event);
            JSONObject body = new JSONObject();
            body.set("msgtype", "markdown");

            JSONObject markdown = new JSONObject();
            markdown.set("content", message);
            body.set("markdown", markdown);

            String response = HttpRequest.post(alarmConfig.getWechatWebhook())
                    .header("Content-Type", "application/json")
                    .body(JSONUtil.toJsonStr(body))
                    .timeout(5000)
                    .execute()
                    .body();

            log.info("WeChat alarm sent successfully for pool [{}], response: {}", event.getPoolName(), response);
        } catch (Exception e) {
            log.error("Failed to send WeChat alarm for pool [{}]", event.getPoolName(), e);
        }
    }

    @Override
    public String getType() {
        return "wechat";
    }

    /**
     * 构建 Markdown 格式消息
     */
    private String buildMarkdownMessage(AlarmEvent event) {
        StringBuilder sb = new StringBuilder();
        sb.append("## 🚨 线程池告警\n");
        
        // 应用信息
        sb.append("> **应用名称**: <font color=\"info\">").append(event.getApplicationName()).append("</font>\n");
        sb.append("> **服务IP**: <font color=\"info\">").append(event.getServiceIp()).append("</font>\n");
        sb.append("> **线程池名称**: <font color=\"warning\">").append(event.getPoolName()).append("</font>\n");
        sb.append("\n");
        
        // 告警信息
        sb.append("> **告警类型**: <font color=\"warning\">").append(getAlarmTypeName(event.getAlarmType())).append("</font>\n");
        sb.append("> **告警指标**: ").append(event.getMetricName()).append("\n");
        if (event.getCurrentValue() != null) {
            sb.append("> **当前值**: <font color=\"warning\">").append(formatValue(event.getCurrentValue())).append("</font>\n");
        }
        if (event.getThreshold() != null) {
            sb.append("> **阈值**: ").append(formatValue(event.getThreshold())).append("\n");
        }
        sb.append("> **告警时间**: ").append(event.getAlarmTime()).append("\n");
        sb.append("\n");
        
        // 详细信息
        sb.append("**详细信息**: ").append(event.getMessage()).append("\n");
        
        // 如果有完整指标，显示其他相关信息
        if (event.getMetrics() != null) {
            sb.append("\n");
            sb.append("### 📊 线程池当前状态\n");
            var m = event.getMetrics();
            sb.append("> 核心线程数: ").append(m.getCorePoolSize()).append("\n");
            sb.append("> 最大线程数: ").append(m.getMaxPoolSize()).append("\n");
            sb.append("> 当前线程数: ").append(m.getPoolSize()).append("\n");
            sb.append("> 活跃线程数: ").append(m.getActiveThreadCount()).append("\n");
            sb.append("> 队列容量: ").append(m.getQueueCapacity()).append("\n");
            sb.append("> 队列大小: ").append(m.getQueueSize()).append("\n");
            sb.append("> 队列使用率: ").append(String.format("%.2f%%", m.getQueueUsageRatio() * 100)).append("\n");
            sb.append("> 活跃线程比例: ").append(String.format("%.2f%%", m.getActiveThreadRatio() * 100)).append("\n");
            sb.append("> 已完成任务数: ").append(m.getCompletedTaskCount()).append("\n");
            sb.append("> 拒绝次数: <font color=\"").append(m.getRejectCount() > 0 ? "warning" : "info").append("\">").append(m.getRejectCount()).append("</font>\n");
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
