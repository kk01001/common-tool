package io.github.kk01001.redisson.example.controller;

import io.github.kk01001.redisson.template.MultiRedissonTemplate;
import jakarta.annotation.PreDestroy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author kk01001
 * @date 2026-01-17 11:30:00
 * @description 延迟队列 Demo - 基于 ZSet 实现，订单超时、定时任务实战案例
 */
@RestController
@RequestMapping("/api/delay-queue")
public class DelayQueueController {

    private static final String ZSET_KEY = "demo:delay:zset";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final MultiRedissonTemplate redissonTemplate;

    /**
     * 消费者线程（定时轮询）
     */
    private ScheduledExecutorService consumerExecutor;
    private final AtomicBoolean running = new AtomicBoolean(false);

    /**
     * 已处理的消息记录
     */
    private final List<ProcessedMessage> processedMessages = new CopyOnWriteArrayList<>();
    private final AtomicLong totalProcessed = new AtomicLong(0);

    public DelayQueueController(MultiRedissonTemplate redissonTemplate) {
        this.redissonTemplate = redissonTemplate;
    }

    @PreDestroy
    public void destroy() {
        stopConsumer();
    }

    /**
     * 添加延迟消息
     *
     * @param message 消息内容
     * @param delay   延迟时间
     * @param unit    时间单位（SECONDS/MINUTES/HOURS）
     */
    @PostMapping("/add")
    public Map<String, Object> addDelayMessage(
            @RequestParam String message,
            @RequestParam(defaultValue = "10") long delay,
            @RequestParam(defaultValue = "SECONDS") String unit) {

        Map<String, Object> result = new LinkedHashMap<>();

        TimeUnit timeUnit = TimeUnit.valueOf(unit.toUpperCase());
        String now = LocalDateTime.now().format(FORMATTER);
        long executeTime = System.currentTimeMillis() + timeUnit.toMillis(delay);
        String expectedTime = LocalDateTime.now().plus(delay, toChronoUnit(timeUnit)).format(FORMATTER);

        // 构建消息（包含创建时间和预期执行时间）
        String fullMessage = String.format("%s|%s|%s|%d", message, now, expectedTime, System.nanoTime());

        // 添加到 ZSet，score 为执行时间戳
        redissonTemplate.zadd(ZSET_KEY, fullMessage, executeTime);

        result.put("success", true);
        result.put("message", message);
        result.put("addTime", now);
        result.put("expectedExecuteTime", expectedTime);
        result.put("delay", delay);
        result.put("unit", unit);

        return result;
    }

    /**
     * 添加订单超时取消任务
     */
    @PostMapping("/order-timeout")
    public Map<String, Object> addOrderTimeout(
            @RequestParam String orderId,
            @RequestParam(defaultValue = "30") long timeoutSeconds) {

        Map<String, Object> result = new LinkedHashMap<>();

        String now = LocalDateTime.now().format(FORMATTER);
        long executeTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(timeoutSeconds);
        String expectedTime = LocalDateTime.now().plusSeconds(timeoutSeconds).format(FORMATTER);

        // 构建订单超时消息
        String message = String.format("ORDER_TIMEOUT|%s|%s|%s|%d", orderId, now, expectedTime, System.nanoTime());

        redissonTemplate.zadd(ZSET_KEY, message, executeTime);

        result.put("success", true);
        result.put("type", "ORDER_TIMEOUT");
        result.put("orderId", orderId);
        result.put("createTime", now);
        result.put("timeoutTime", expectedTime);
        result.put("timeoutSeconds", timeoutSeconds);
        result.put("message", String.format("订单 %s 将在 %d 秒后自动取消（如未支付）", orderId, timeoutSeconds));

        return result;
    }

    /**
     * 添加定时提醒任务
     */
    @PostMapping("/reminder")
    public Map<String, Object> addReminder(
            @RequestParam String content,
            @RequestParam String userId,
            @RequestParam(defaultValue = "5") long delaySeconds) {

        Map<String, Object> result = new LinkedHashMap<>();

        String now = LocalDateTime.now().format(FORMATTER);
        long executeTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(delaySeconds);
        String expectedTime = LocalDateTime.now().plusSeconds(delaySeconds).format(FORMATTER);

        String message = String.format("REMINDER|%s|%s|%s|%s|%d", userId, content, now, expectedTime, System.nanoTime());

        redissonTemplate.zadd(ZSET_KEY, message, executeTime);

        result.put("success", true);
        result.put("type", "REMINDER");
        result.put("userId", userId);
        result.put("content", content);
        result.put("createTime", now);
        result.put("reminderTime", expectedTime);

        return result;
    }

    /**
     * 启动消费者
     */
    @PostMapping("/consumer/start")
    public Map<String, Object> startConsumer() {
        Map<String, Object> result = new LinkedHashMap<>();

        if (running.get()) {
            result.put("success", false);
            result.put("message", "消费者已在运行中");
            return result;
        }

        running.set(true);
        consumerExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "delay-queue-consumer");
            t.setDaemon(true);
            return t;
        });

        // 每 500ms 轮询一次
        consumerExecutor.scheduleAtFixedRate(this::pollAndProcess, 0, 500, TimeUnit.MILLISECONDS);

        result.put("success", true);
        result.put("message", "消费者已启动（轮询间隔 500ms）");
        result.put("status", "RUNNING");

        return result;
    }

    /**
     * 停止消费者
     */
    @PostMapping("/consumer/stop")
    public Map<String, Object> stopConsumer() {
        Map<String, Object> result = new LinkedHashMap<>();

        if (!running.get()) {
            result.put("success", false);
            result.put("message", "消费者未运行");
            return result;
        }

        running.set(false);

        if (consumerExecutor != null) {
            consumerExecutor.shutdownNow();
            consumerExecutor = null;
        }

        result.put("success", true);
        result.put("message", "消费者已停止");
        result.put("status", "STOPPED");

        return result;
    }

    /**
     * 获取队列状态
     */
    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        Map<String, Object> result = new LinkedHashMap<>();

        result.put("consumerRunning", running.get());
        result.put("queueSize", redissonTemplate.zcard(ZSET_KEY));
        result.put("totalProcessed", totalProcessed.get());

        // 最近处理的消息
        int showCount = Math.min(10, processedMessages.size());
        List<Map<String, Object>> recentMessages = new ArrayList<>();
        for (int i = processedMessages.size() - 1; i >= Math.max(0, processedMessages.size() - showCount); i--) {
            ProcessedMessage msg = processedMessages.get(i);
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("message", msg.message);
            m.put("processedTime", msg.processedTime);
            m.put("actualDelay", msg.actualDelay);
            recentMessages.add(m);
        }
        result.put("recentProcessed", recentMessages);

        return result;
    }

    /**
     * 查看待处理消息（不消费）
     */
    @GetMapping("/pending")
    public Map<String, Object> getPendingMessages() {
        Map<String, Object> result = new LinkedHashMap<>();

        Collection<String> pending = redissonTemplate.zgetAll(ZSET_KEY);

        result.put("count", pending.size());
        result.put("messages", pending);

        return result;
    }

    /**
     * 清空队列
     */
    @PostMapping("/clear")
    public Map<String, Object> clearQueue() {
        Map<String, Object> result = new LinkedHashMap<>();

        redissonTemplate.zclear(ZSET_KEY);
        processedMessages.clear();
        totalProcessed.set(0);

        result.put("success", true);
        result.put("message", "队列已清空");

        return result;
    }

    /**
     * 轮询并处理到期消息
     */
    private void pollAndProcess() {
        if (!running.get()) {
            return;
        }

        try {
            long now = System.currentTimeMillis();

            // 获取所有到期的消息（score <= 当前时间）
            Collection<String> expiredMessages = redissonTemplate.zrangeByScore(ZSET_KEY, 0, now);

            for (String message : expiredMessages) {
                // 尝试移除（原子操作，防止重复消费）
                boolean removed = redissonTemplate.zremove(ZSET_KEY, message);
                if (removed) {
                    processMessage(message);
                }
            }
        } catch (Exception e) {
            // 记录错误但继续轮询
        }
    }

    /**
     * 处理消息
     */
    private void processMessage(String message) {
        String processedTime = LocalDateTime.now().format(FORMATTER);
        String[] parts = message.split("\\|");

        String actualDelay = "N/A";
        if (parts.length >= 3) {
            try {
                // 解析预期执行时间
                String expectedTimeStr = parts[parts.length - 2];
                LocalDateTime expected = LocalDateTime.parse(expectedTimeStr, FORMATTER);
                LocalDateTime actual = LocalDateTime.now();
                long diffMs = java.time.Duration.between(expected, actual).toMillis();
                actualDelay = diffMs + "ms " + (diffMs >= 0 ? "(延迟)" : "(提前)");
            } catch (Exception ignored) {
            }
        }

        // 根据消息类型处理
        if (message.startsWith("ORDER_TIMEOUT|")) {
            // 处理订单超时
            String orderId = parts.length > 1 ? parts[1] : "unknown";
            // 实际业务中这里会调用订单服务取消订单
            System.out.println("[订单超时] 订单 " + orderId + " 已超时，执行取消操作");
        } else if (message.startsWith("REMINDER|")) {
            // 处理提醒
            String userId = parts.length > 1 ? parts[1] : "unknown";
            String content = parts.length > 2 ? parts[2] : "";
            // 实际业务中这里会发送提醒通知
            System.out.println("[提醒] 用户 " + userId + ": " + content);
        } else {
            System.out.println("[延迟消息] " + parts[0]);
        }

        // 记录处理结果
        processedMessages.add(new ProcessedMessage(message, processedTime, actualDelay));
        totalProcessed.incrementAndGet();

        // 保持最近100条记录
        while (processedMessages.size() > 100) {
            processedMessages.remove(0);
        }
    }

    private java.time.temporal.ChronoUnit toChronoUnit(TimeUnit unit) {
        return switch (unit) {
            case SECONDS -> java.time.temporal.ChronoUnit.SECONDS;
            case MINUTES -> java.time.temporal.ChronoUnit.MINUTES;
            case HOURS -> java.time.temporal.ChronoUnit.HOURS;
            case DAYS -> java.time.temporal.ChronoUnit.DAYS;
            default -> java.time.temporal.ChronoUnit.SECONDS;
        };
    }

    /**
     * 已处理消息记录
     */
    private record ProcessedMessage(String message, String processedTime, String actualDelay) {
    }
}
