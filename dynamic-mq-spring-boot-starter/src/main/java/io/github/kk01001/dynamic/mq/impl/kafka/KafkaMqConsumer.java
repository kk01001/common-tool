package io.github.kk01001.dynamic.mq.impl.kafka;

import io.github.kk01001.dynamic.mq.config.DynamicMqProperties;
import io.github.kk01001.dynamic.mq.core.AbstractMqConsumer;
import io.github.kk01001.dynamic.mq.model.MqMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author linshiqiang
 * @date 2025-06-21 20:48:56
 * @description Kafka消费者实现
 */
@Slf4j
@RequiredArgsConstructor
public class KafkaMqConsumer extends AbstractMqConsumer {
    
    private final DynamicMqProperties properties;
    
    private final Map<String, KafkaConsumer<String, String>> consumers = new ConcurrentHashMap<>();
    private final Map<String, MqMessageHandler> messageHandlers = new ConcurrentHashMap<>();
    private final Map<String, AtomicBoolean> consumerRunning = new ConcurrentHashMap<>();
    private ExecutorService executorService;
    
    @Override
    protected void doStart() {
        try {
            initExecutorService();
            // 启动所有已注册的消费者
            consumerRunning.values().forEach(running -> running.set(true));
            log.info("Kafka消费者已启动");
        } catch (Exception e) {
            log.error("Kafka消费者启动失败", e);
            throw new RuntimeException("Kafka消费者启动失败", e);
        }
    }
    
    @Override
    protected void doStop() {
        // 停止所有消费者
        consumerRunning.values().forEach(running -> running.set(false));
        consumers.values().forEach(KafkaConsumer::close);
        consumers.clear();
        messageHandlers.clear();
        consumerRunning.clear();
        
        if (executorService != null) {
            executorService.shutdown();
        }
        
        log.info("Kafka消费者已停止");
    }
    
    @Override
    protected void doSubscribe(String topic, String tag, MqMessageHandler messageHandler) {
        try {
            String key = topic + ":" + (StringUtils.hasText(tag) ? tag : "");
            
            // 创建消费者
            KafkaConsumer<String, String> consumer = createConsumer();
            consumer.subscribe(Collections.singletonList(topic));
            
            consumers.put(key, consumer);
            messageHandlers.put(key, messageHandler);
            
            AtomicBoolean consumerRunningFlag = new AtomicBoolean(running);
            consumerRunning.put(key, consumerRunningFlag);
            
            // 启动消费线程
            executorService.submit(() -> {
                while (consumerRunningFlag.get()) {
                    try {
                        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                        
                        for (ConsumerRecord<String, String> record : records) {
                            try {
                                // 如果指定了tag，需要过滤
                                if (StringUtils.hasText(tag) && !tag.equals(record.key())) {
                                    continue;
                                }
                                
                                MqMessage mqMessage = convertToMqMessage(record);
                                boolean success = messageHandler.handle(mqMessage);
                                
                                if (!success) {
                                    log.warn("消息处理失败，将重试: topic={}, partition={}, offset={}", 
                                            record.topic(), record.partition(), record.offset());
                                }
                            } catch (Exception e) {
                                log.error("处理Kafka消息失败: topic={}, partition={}, offset={}", 
                                        record.topic(), record.partition(), record.offset(), e);
                            }
                        }
                        
                        // 手动提交偏移量
                        consumer.commitSync();
                        
                    } catch (Exception e) {
                        if (consumerRunningFlag.get()) {
                            log.error("Kafka消费异常", e);
                        }
                    }
                }
            });
            
            log.info("Kafka订阅成功: topic={}, tag={}", topic, tag);
        } catch (Exception e) {
            log.error("Kafka订阅失败: topic={}, tag={}", topic, tag, e);
            throw new RuntimeException("Kafka订阅失败", e);
        }
    }
    
    @Override
    protected void doUnsubscribe(String topic) {
        consumers.entrySet().removeIf(entry -> {
            if (entry.getKey().startsWith(topic + ":")) {
                String key = entry.getKey();
                consumerRunning.get(key).set(false);
                entry.getValue().close();
                consumerRunning.remove(key);
                return true;
            }
            return false;
        });
        messageHandlers.entrySet().removeIf(entry -> entry.getKey().startsWith(topic + ":"));
        log.info("Kafka取消订阅: topic={}", topic);
    }
    
    @Override
    public String getConsumerType() {
        return "kafka";
    }
    
    private void initExecutorService() {
        this.executorService = Executors.newCachedThreadPool(r -> {
            Thread thread = new Thread(r, "kafka-consumer-thread");
            thread.setDaemon(true);
            return thread;
        });
    }
    
    private KafkaConsumer<String, String> createConsumer() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getKafka().getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, properties.getConsumer().getGroupName());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, properties.getConsumer().getConsumeMessageBatchMaxSize());
        
        // 添加自定义配置
        props.putAll(properties.getKafka().getProperties());
        
        return new KafkaConsumer<>(props);
    }
    
    private MqMessage convertToMqMessage(ConsumerRecord<String, String> record) {
        return MqMessage.builder()
                .messageId(record.topic() + "-" + record.partition() + "-" + record.offset())
                .topic(record.topic())
                .tag(record.key())
                .payload(record.value())
                .createTime(record.timestamp())
                .build();
    }
}
