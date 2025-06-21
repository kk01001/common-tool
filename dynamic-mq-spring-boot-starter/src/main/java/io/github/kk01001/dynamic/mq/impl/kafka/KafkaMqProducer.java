package io.github.kk01001.dynamic.mq.impl.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.kk01001.dynamic.mq.config.DynamicMqProperties;
import io.github.kk01001.dynamic.mq.core.AbstractMqProducer;
import io.github.kk01001.dynamic.mq.core.AdvancedMqProducer;
import io.github.kk01001.dynamic.mq.core.MqSendCallback;
import io.github.kk01001.dynamic.mq.core.MqTransactionListener;
import io.github.kk01001.dynamic.mq.enums.TransactionState;
import io.github.kk01001.dynamic.mq.model.MqMessage;
import io.github.kk01001.dynamic.mq.model.MqSendResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.Properties;
import java.util.concurrent.Future;

/**
 * @author linshiqiang
 * @date 2025-06-21 20:48:56
 * @description Kafka生产者实现
 */
@Slf4j
@RequiredArgsConstructor
public class KafkaMqProducer extends AbstractMqProducer implements AdvancedMqProducer {
    
    private final DynamicMqProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private KafkaProducer<String, String> producer;
    private KafkaProducer<String, String> transactionProducer;
    
    @PostConstruct
    public void init() {
        try {
            initProducer();
            initTransactionProducer();
            log.info("Kafka生产者初始化成功");
        } catch (Exception e) {
            log.error("Kafka生产者初始化失败", e);
            throw new RuntimeException("Kafka生产者初始化失败", e);
        }
    }
    
    @PreDestroy
    public void destroy() {
        try {
            if (producer != null) {
                producer.close();
            }
            if (transactionProducer != null) {
                transactionProducer.close();
            }
            log.info("Kafka生产者资源清理完成");
        } catch (Exception e) {
            log.error("Kafka生产者资源清理失败", e);
        }
    }
    
    private void initProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getKafka().getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, properties.getProducer().getMaxRetryTimes());
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, (int) properties.getProducer().getSendTimeout());
        
        // 添加自定义配置
        props.putAll(properties.getKafka().getProperties());
        
        this.producer = new KafkaProducer<>(props);
    }
    
    @Override
    protected boolean doSend(MqMessage message) {
        try {
            String key = StringUtils.hasText(message.getTag()) ? message.getTag() : null;
            String value = objectMapper.writeValueAsString(message.getPayload());
            
            ProducerRecord<String, String> record = new ProducerRecord<>(
                    message.getTopic(), 
                    key, 
                    value
            );
            
            // 添加消息头
            if (message.getHeaders() != null) {
                message.getHeaders().forEach((k, v) -> 
                    record.headers().add(k, v.toString().getBytes())
                );
            }
            
            Future<RecordMetadata> future = producer.send(record);
            RecordMetadata metadata = future.get();
            
            log.debug("Kafka消息发送成功: topic={}, partition={}, offset={}", 
                    metadata.topic(), metadata.partition(), metadata.offset());
            
            return true;
        } catch (Exception e) {
            log.error("Kafka发送消息失败", e);
            return false;
        }
    }
    
    @Override
    protected boolean doBatchSend(MqMessage... messages) {
        try {
            for (MqMessage message : messages) {
                if (!doSend(message)) {
                    return false;
                }
            }
            // 刷新缓冲区
            producer.flush();
            return true;
        } catch (Exception e) {
            log.error("Kafka批量发送消息失败", e);
            return false;
        }
    }
    
    /**
     * 初始化事务生产者
     */
    private void initTransactionProducer() {
        if (properties.getProducer().isTransactionEnabled()) {
            Properties props = new Properties();
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getKafka().getBootstrapServers());
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            props.put(ProducerConfig.ACKS_CONFIG, "all");
            props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
            props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "kafka-transaction-producer");

            // 添加自定义配置
            props.putAll(properties.getKafka().getProperties());

            this.transactionProducer = new KafkaProducer<>(props);
            this.transactionProducer.initTransactions();
        }
    }

    // ==================== 新增方法实现 ====================

    @Override
    public void sendAsync(MqMessage message, MqSendCallback callback) {
        try {
            String key = StringUtils.hasText(message.getTag()) ? message.getTag() : null;
            String value = objectMapper.writeValueAsString(message.getPayload());

            ProducerRecord<String, String> record = new ProducerRecord<>(
                    message.getTopic(),
                    key,
                    value
            );

            // 添加消息头
            if (message.getHeaders() != null) {
                message.getHeaders().forEach((k, v) ->
                    record.headers().add(k, v.toString().getBytes())
                );
            }

            // 异步发送
            producer.send(record, (metadata, exception) -> {
                if (exception == null) {
                    MqSendResult result = MqSendResult.builder()
                            .messageId(message.getMessageId())
                            .topic(metadata.topic())
                            .status("SUCCESS")
                            .queueId(String.valueOf(metadata.partition()))
                            .queueOffset(metadata.offset())
                            .sendTimestamp(metadata.timestamp())
                            .build();
                    callback.onSuccess(result);
                } else {
                    callback.onException(exception);
                }
            });

        } catch (Exception e) {
            callback.onException(e);
        }
    }

    @Override
    public void sendOneWay(MqMessage message) {
        try {
            String key = StringUtils.hasText(message.getTag()) ? message.getTag() : null;
            String value = objectMapper.writeValueAsString(message.getPayload());

            ProducerRecord<String, String> record = new ProducerRecord<>(
                    message.getTopic(),
                    key,
                    value
            );

            // 添加消息头
            if (message.getHeaders() != null) {
                message.getHeaders().forEach((k, v) ->
                    record.headers().add(k, v.toString().getBytes())
                );
            }

            // OneWay发送：acks=0，不等待确认
            Properties onewayProps = new Properties();
            onewayProps.putAll(createProducerProperties());
            onewayProps.put(ProducerConfig.ACKS_CONFIG, "0");

            KafkaProducer<String, String> onewayProducer = new KafkaProducer<>(onewayProps);
            onewayProducer.send(record);
            onewayProducer.close();

        } catch (Exception e) {
            log.warn("Kafka OneWay发送失败，忽略异常: {}", e.getMessage());
        }
    }

    @Override
    public boolean sendOrderly(MqMessage message, String orderKey) {
        try {
            // Kafka顺序消息：使用相同的分区键保证顺序
            String value = objectMapper.writeValueAsString(message.getPayload());

            ProducerRecord<String, String> record = new ProducerRecord<>(
                    message.getTopic(),
                    orderKey,  // 使用orderKey作为分区键
                    value
            );

            // 添加消息头
            if (message.getHeaders() != null) {
                message.getHeaders().forEach((k, v) ->
                    record.headers().add(k, v.toString().getBytes())
                );
            }
            record.headers().add("orderKey", orderKey.getBytes());

            Future<RecordMetadata> future = producer.send(record);
            RecordMetadata metadata = future.get();

            log.debug("Kafka顺序消息发送成功: topic={}, partition={}, offset={}, orderKey={}",
                    metadata.topic(), metadata.partition(), metadata.offset(), orderKey);

            return true;
        } catch (Exception e) {
            log.error("Kafka发送顺序消息失败", e);
            return false;
        }
    }

    @Override
    public boolean sendTransaction(MqMessage message, String transactionId, MqTransactionListener transactionListener) {
        if (transactionProducer == null) {
            log.error("事务生产者未初始化，请启用事务功能");
            return false;
        }

        try {
            transactionProducer.beginTransaction();

            try {
                // 执行本地事务
                TransactionState localState = transactionListener.executeLocalTransaction(message, transactionId);

                if (localState == TransactionState.COMMIT) {
                    // 发送消息
                    String key = StringUtils.hasText(message.getTag()) ? message.getTag() : null;
                    String value = objectMapper.writeValueAsString(message.getPayload());

                    ProducerRecord<String, String> record = new ProducerRecord<>(
                            message.getTopic(),
                            key,
                            value
                    );

                    // 添加消息头
                    if (message.getHeaders() != null) {
                        message.getHeaders().forEach((k, v) ->
                            record.headers().add(k, v.toString().getBytes())
                        );
                    }
                    record.headers().add("transactionId", transactionId.getBytes());

                    transactionProducer.send(record);
                    transactionProducer.commitTransaction();
                    return true;
                } else {
                    // 回滚事务
                    transactionProducer.abortTransaction();
                    return false;
                }
            } catch (Exception e) {
                log.error("事务消息处理失败", e);
                transactionProducer.abortTransaction();
                return false;
            }

        } catch (Exception e) {
            log.error("Kafka发送事务消息失败", e);
            return false;
        }
    }

    @Override
    public String getProducerType() {
        return "kafka";
    }

    /**
     * 创建生产者配置
     */
    private Properties createProducerProperties() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getKafka().getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, properties.getProducer().getMaxRetryTimes());
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, (int) properties.getProducer().getSendTimeout());

        // 添加自定义配置
        props.putAll(properties.getKafka().getProperties());

        return props;
    }

    // ==================== AdvancedMqProducer 接口实现 ====================

    @Override
    public boolean supportsAsync() {
        return true;
    }

    @Override
    public boolean supportsOneWay() {
        return true; // 通过acks=0实现
    }

    @Override
    public boolean supportsOrderly() {
        return true; // 通过分区键实现
    }

    @Override
    public boolean supportsTransaction() {
        return properties.getProducer().isTransactionEnabled();
    }

    @Override
    public boolean supportsDelay() {
        return false; // Kafka原生不支持延迟消息
    }

    @Override
    public long[] getSupportedDelayLevels() {
        return new long[0]; // 不支持延迟消息
    }

    @Override
    public int getMaxBatchSize() {
        return 1000; // Kafka建议的批量大小
    }
}
