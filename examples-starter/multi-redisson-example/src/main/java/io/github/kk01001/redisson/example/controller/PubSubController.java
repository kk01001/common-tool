package io.github.archer099.redisson.example.controller;

import io.github.archer099.redisson.template.MultiRedissonTemplate;
import jakarta.annotation.PreDestroy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author archer099
 * @date 2026-01-17 19:30:00
 * @description 发布订阅 Demo - 实时消息推送实战案例
 * <p>
 * 应用场景：
 * 1. 实时通知推送
 * 2. 聊天室消息广播
 * 3. 配置变更通知
 * 4. 缓存失效通知
 * 5. 系统事件广播
 * </p>
 */
@RestController
@RequestMapping("/api/pubsub")
public class PubSubController {

    private static final String TOPIC_PREFIX = "demo:topic:";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private final MultiRedissonTemplate redissonTemplate;

    /**
     * 订阅者 ID 映射
     */
    private final Map<String, Map<String, Integer>> topicListeners = new ConcurrentHashMap<>();

    /**
     * 接收到的消息记录
     */
    private final List<ReceivedMessage> receivedMessages = new CopyOnWriteArrayList<>();

    /**
     * 统计
     */
    private final AtomicLong publishedCount = new AtomicLong(0);
    private final AtomicLong receivedCount = new AtomicLong(0);

    public PubSubController(MultiRedissonTemplate redissonTemplate) {
        this.redissonTemplate = redissonTemplate;
    }

    @PreDestroy
    public void destroy() {
        // 清理所有订阅
        topicListeners.forEach((topic, listeners) -> {
            listeners.values().forEach(listenerId -> 
                redissonTemplate.unsubscribe(TOPIC_PREFIX + topic, listenerId));
        });
    }

    /**
     * 订阅主题
     *
     * @param topic      主题名称
     * @param subscriber 订阅者名称
     */
    @PostMapping("/subscribe")
    public Map<String, Object> subscribe(
            @RequestParam(defaultValue = "news") String topic,
            @RequestParam(defaultValue = "user1") String subscriber) {

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("timestamp", LocalDateTime.now().format(FORMATTER));

        String fullTopic = TOPIC_PREFIX + topic;

        // 检查是否已订阅
        Map<String, Integer> listeners = topicListeners.computeIfAbsent(topic, k -> new ConcurrentHashMap<>());
        if (listeners.containsKey(subscriber)) {
            result.put("success", false);
            result.put("message", String.format("订阅者 %s 已订阅主题 %s", subscriber, topic));
            return result;
        }

        // 添加消息监听器
        int listenerId = redissonTemplate.subscribe(fullTopic, String.class, (channel, msg) -> {
            String receiveTime = LocalDateTime.now().format(FORMATTER);
            ReceivedMessage received = new ReceivedMessage(topic, subscriber, msg, receiveTime);
            receivedMessages.add(received);
            receivedCount.incrementAndGet();

            // 保留最近100条
            while (receivedMessages.size() > 100) {
                receivedMessages.remove(0);
            }

            System.out.printf("[PubSub] 订阅者 %s 收到消息: %s%n", subscriber, msg);
        });

        listeners.put(subscriber, listenerId);

        result.put("success", true);
        result.put("topic", topic);
        result.put("subscriber", subscriber);
        result.put("subscriberCount", listeners.size());
        result.put("message", String.format("订阅者 %s 成功订阅主题 %s", subscriber, topic));

        return result;
    }

    /**
     * 取消订阅
     */
    @PostMapping("/unsubscribe")
    public Map<String, Object> unsubscribe(
            @RequestParam(defaultValue = "news") String topic,
            @RequestParam(defaultValue = "user1") String subscriber) {

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("timestamp", LocalDateTime.now().format(FORMATTER));

        Map<String, Integer> listeners = topicListeners.get(topic);
        if (listeners == null || !listeners.containsKey(subscriber)) {
            result.put("success", false);
            result.put("message", String.format("订阅者 %s 未订阅主题 %s", subscriber, topic));
            return result;
        }

        String fullTopic = TOPIC_PREFIX + topic;
        redissonTemplate.unsubscribe(fullTopic, listeners.remove(subscriber));

        result.put("success", true);
        result.put("topic", topic);
        result.put("subscriber", subscriber);
        result.put("message", String.format("订阅者 %s 已取消订阅主题 %s", subscriber, topic));

        return result;
    }

    /**
     * 发布消息
     *
     * @param topic   主题名称
     * @param message 消息内容
     */
    @PostMapping("/publish")
    public Map<String, Object> publish(
            @RequestParam(defaultValue = "news") String topic,
            @RequestParam String message) {

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("timestamp", LocalDateTime.now().format(FORMATTER));

        String fullTopic = TOPIC_PREFIX + topic;

        // 发布消息，返回接收者数量
        long receivers = redissonTemplate.publish(fullTopic, message);
        publishedCount.incrementAndGet();

        result.put("success", true);
        result.put("topic", topic);
        result.put("message", message);
        result.put("receivers", receivers);
        result.put("totalPublished", publishedCount.get());

        return result;
    }

    /**
     * 批量发布消息
     */
    @PostMapping("/publish/batch")
    public Map<String, Object> publishBatch(
            @RequestParam(defaultValue = "news") String topic,
            @RequestParam(defaultValue = "5") int count) {

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("timestamp", LocalDateTime.now().format(FORMATTER));

        String fullTopic = TOPIC_PREFIX + topic;

        long totalReceivers = 0;
        for (int i = 1; i <= count; i++) {
            String message = String.format("批量消息 #%d - %s", i, LocalDateTime.now().format(FORMATTER));
            totalReceivers += redissonTemplate.publish(fullTopic, message);
            publishedCount.incrementAndGet();
        }

        result.put("success", true);
        result.put("topic", topic);
        result.put("publishedCount", count);
        result.put("totalReceivers", totalReceivers);

        return result;
    }

    /**
     * 获取主题状态
     */
    @GetMapping("/status")
    public Map<String, Object> getStatus(@RequestParam(defaultValue = "news") String topic) {
        Map<String, Object> result = new LinkedHashMap<>();

        String fullTopic = TOPIC_PREFIX + topic;
        Map<String, Integer> listeners = topicListeners.getOrDefault(topic, new ConcurrentHashMap<>());

        result.put("topic", topic);
        result.put("subscriberCount", redissonTemplate.countSubscribers(fullTopic));
        result.put("localSubscribers", listeners.keySet());
        result.put("totalPublished", publishedCount.get());
        result.put("totalReceived", receivedCount.get());

        return result;
    }

    /**
     * 获取最近收到的消息
     */
    @GetMapping("/messages")
    public Map<String, Object> getMessages(
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) String subscriber,
            @RequestParam(defaultValue = "20") int limit) {

        Map<String, Object> result = new LinkedHashMap<>();

        List<ReceivedMessage> filtered = receivedMessages.stream()
                .filter(m -> topic == null || m.topic.equals(topic))
                .filter(m -> subscriber == null || m.subscriber.equals(subscriber))
                .toList();

        int fromIndex = Math.max(0, filtered.size() - limit);
        List<ReceivedMessage> recent = filtered.subList(fromIndex, filtered.size());

        result.put("count", recent.size());
        result.put("messages", recent.reversed());

        return result;
    }

    /**
     * 获取所有主题订阅情况
     */
    @GetMapping("/topics")
    public Map<String, Object> getAllTopics() {
        Map<String, Object> result = new LinkedHashMap<>();

        Map<String, Object> topics = new LinkedHashMap<>();
        topicListeners.forEach((topic, listeners) -> {
            Map<String, Object> topicInfo = new LinkedHashMap<>();
            topicInfo.put("subscriberCount", listeners.size());
            topicInfo.put("subscribers", listeners.keySet());
            topics.put(topic, topicInfo);
        });

        result.put("topics", topics);
        result.put("totalPublished", publishedCount.get());
        result.put("totalReceived", receivedCount.get());

        return result;
    }

    /**
     * 清空记录
     */
    @PostMapping("/clear")
    public Map<String, Object> clear() {
        Map<String, Object> result = new LinkedHashMap<>();

        receivedMessages.clear();
        publishedCount.set(0);
        receivedCount.set(0);

        result.put("success", true);
        result.put("message", "统计已清空");

        return result;
    }

    /**
     * 接收到的消息记录
     */
    private record ReceivedMessage(String topic, String subscriber, String message, String receiveTime) {
    }
}
