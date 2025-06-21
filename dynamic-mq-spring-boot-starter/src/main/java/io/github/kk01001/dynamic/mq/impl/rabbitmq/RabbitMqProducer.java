package io.github.kk01001.dynamic.mq.impl.rabbitmq;

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
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * @author linshiqiang
 * @date 2025-06-21 20:48:56
 * @description RabbitMQ生产者实现
 */
@Slf4j
@RequiredArgsConstructor
public class RabbitMqProducer extends AbstractMqProducer implements AdvancedMqProducer {
    
    private final DynamicMqProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private ConnectionFactory connectionFactory;
    private RabbitTemplate rabbitTemplate;
    private RabbitTemplate transactionTemplate;
    private AmqpAdmin amqpAdmin;
    
    @PostConstruct
    public void init() {
        try {
            initConnectionFactory();
            initRabbitTemplate();
            initTransactionTemplate();
            initAmqpAdmin();
            log.info("RabbitMQ生产者初始化成功");
        } catch (Exception e) {
            log.error("RabbitMQ生产者初始化失败", e);
            throw new RuntimeException("RabbitMQ生产者初始化失败", e);
        }
    }
    
    @PreDestroy
    public void destroy() {
        try {
            if (connectionFactory instanceof CachingConnectionFactory) {
                ((CachingConnectionFactory) connectionFactory).destroy();
            }
            log.info("RabbitMQ生产者资源清理完成");
        } catch (Exception e) {
            log.error("RabbitMQ生产者资源清理失败", e);
        }
    }
    
    private void initConnectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setHost(properties.getRabbitmq().getHost());
        factory.setPort(properties.getRabbitmq().getPort());
        factory.setUsername(properties.getRabbitmq().getUsername());
        factory.setPassword(properties.getRabbitmq().getPassword());
        factory.setVirtualHost(properties.getRabbitmq().getVirtualHost());
        this.connectionFactory = factory;
    }
    
    private void initRabbitTemplate() {
        this.rabbitTemplate = new RabbitTemplate(connectionFactory);
        this.rabbitTemplate.setMandatory(true);
        
        // 设置确认回调
        this.rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.debug("消息发送确认成功: {}", correlationData);
            } else {
                log.error("消息发送确认失败: {}, cause: {}", correlationData, cause);
            }
        });
        
        // 设置返回回调
        this.rabbitTemplate.setReturnsCallback(returned -> {
            log.error("消息发送失败返回: {}", returned);
        });
    }
    
    private void initAmqpAdmin() {
        this.amqpAdmin = new RabbitAdmin(connectionFactory);
    }
    
    @Override
    protected boolean doSend(MqMessage message) {
        try {
            // 确保交换机和队列存在
            ensureExchangeAndQueue(message.getTopic(), message.getTag());
            
            // 构建消息属性
            MessageProperties messageProperties = new MessageProperties();
            messageProperties.setContentType("application/json");
            messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            
            if (message.getDelayTime() != null && message.getDelayTime() > 0) {
                // RabbitMQ延迟消息需要插件支持，使用消息头设置延迟时间
                messageProperties.setHeader("x-delay", message.getDelayTime().intValue());
            }
            
            // 序列化消息体
            String jsonPayload = objectMapper.writeValueAsString(message.getPayload());
            Message rabbitMessage = new Message(jsonPayload.getBytes(), messageProperties);
            
            // 发送消息
            String routingKey = StringUtils.hasText(message.getTag()) ? message.getTag() : "";
            rabbitTemplate.send(message.getTopic(), routingKey, rabbitMessage);
            
            return true;
        } catch (Exception e) {
            log.error("RabbitMQ发送消息失败", e);
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
            return true;
        } catch (Exception e) {
            log.error("RabbitMQ批量发送消息失败", e);
            return false;
        }
    }
    

    
    @Override
    public String getProducerType() {
        return "rabbitmq";
    }
    
    private void ensureExchangeAndQueue(String topic, String tag) {
        // 创建交换机
        Exchange exchange = new TopicExchange(topic, true, false);
        amqpAdmin.declareExchange(exchange);
        
        // 创建队列
        String queueName = buildQueueName(topic, tag);
        Queue queue = new Queue(queueName, true, false, false);
        amqpAdmin.declareQueue(queue);
        
        // 绑定队列到交换机
        String routingKey = StringUtils.hasText(tag) ? tag : "#";
        Binding binding = BindingBuilder.bind(queue).to(exchange).with(routingKey).noargs();
        amqpAdmin.declareBinding(binding);
    }
    
    private String buildQueueName(String topic, String tag) {
        String groupName = properties.getConsumer().getGroupName();
        if (StringUtils.hasText(tag)) {
            return String.format("%s.%s.%s", topic, tag, groupName);
        } else {
            return String.format("%s.%s", topic, groupName);
        }
    }

    /**
     * 初始化事务模板
     */
    private void initTransactionTemplate() {
        if (properties.getProducer().isTransactionEnabled()) {
            this.transactionTemplate = new RabbitTemplate(connectionFactory);
            this.transactionTemplate.setChannelTransacted(true);
            this.transactionTemplate.setMandatory(true);
        }
    }

    // ==================== 新增方法实现 ====================

    @Override
    public void sendAsync(MqMessage message, MqSendCallback callback) {
        try {
            // RabbitMQ异步发送实现
            ensureExchangeAndQueue(message.getTopic(), message.getTag());

            MessageProperties messageProperties = new MessageProperties();
            messageProperties.setContentType("application/json");
            messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);

            if (message.getDelayTime() != null && message.getDelayTime() > 0) {
                // RabbitMQ延迟消息需要插件支持，使用消息头设置延迟时间
                messageProperties.setHeader("x-delay", message.getDelayTime().intValue());
            }

            String jsonPayload = objectMapper.writeValueAsString(message.getPayload());
            Message rabbitMessage = new Message(jsonPayload.getBytes(), messageProperties);
            String routingKey = StringUtils.hasText(message.getTag()) ? message.getTag() : "";

            // 设置异步回调
            rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
                if (ack) {
                    MqSendResult result = MqSendResult.success(message.getMessageId(), message.getTopic());
                    callback.onSuccess(result);
                } else {
                    callback.onException(new RuntimeException("发送确认失败: " + cause));
                }
            });

            rabbitTemplate.send(message.getTopic(), routingKey, rabbitMessage);

        } catch (Exception e) {
            callback.onException(e);
        }
    }

    @Override
    public void sendOneWay(MqMessage message) {
        try {
            // RabbitMQ OneWay发送：不等待确认
            ensureExchangeAndQueue(message.getTopic(), message.getTag());

            MessageProperties messageProperties = new MessageProperties();
            messageProperties.setContentType("application/json");
            messageProperties.setDeliveryMode(MessageDeliveryMode.NON_PERSISTENT); // 非持久化提高性能

            String jsonPayload = objectMapper.writeValueAsString(message.getPayload());
            Message rabbitMessage = new Message(jsonPayload.getBytes(), messageProperties);
            String routingKey = StringUtils.hasText(message.getTag()) ? message.getTag() : "";

            // 创建临时模板，不设置确认回调
            RabbitTemplate onewayTemplate = new RabbitTemplate(connectionFactory);
            onewayTemplate.setMandatory(false);
            onewayTemplate.send(message.getTopic(), routingKey, rabbitMessage);

        } catch (Exception e) {
            log.warn("RabbitMQ OneWay发送失败，忽略异常: {}", e.getMessage());
        }
    }

    @Override
    public boolean sendOrderly(MqMessage message, String orderKey) {
        try {
            // RabbitMQ顺序消息：使用单一队列和单一消费者
            String orderTopic = message.getTopic() + ".order";
            String orderQueue = orderTopic + ".queue";

            // 创建顺序队列
            ensureOrderQueue(orderTopic, orderQueue);

            MessageProperties messageProperties = new MessageProperties();
            messageProperties.setContentType("application/json");
            messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            messageProperties.setHeader("orderKey", orderKey);

            String jsonPayload = objectMapper.writeValueAsString(message.getPayload());
            Message rabbitMessage = new Message(jsonPayload.getBytes(), messageProperties);

            rabbitTemplate.send(orderTopic, "", rabbitMessage);
            return true;

        } catch (Exception e) {
            log.error("RabbitMQ发送顺序消息失败", e);
            return false;
        }
    }

    @Override
    public boolean sendTransaction(MqMessage message, String transactionId, MqTransactionListener transactionListener) {
        if (transactionTemplate == null) {
            log.error("事务模板未初始化，请启用事务功能");
            return false;
        }

        try {
            // 执行本地事务
            TransactionState localState = transactionListener.executeLocalTransaction(message, transactionId);

            if (localState == TransactionState.COMMIT) {
                // 发送消息
                ensureExchangeAndQueue(message.getTopic(), message.getTag());

                MessageProperties messageProperties = new MessageProperties();
                messageProperties.setContentType("application/json");
                messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                messageProperties.setHeader("transactionId", transactionId);

                String jsonPayload = objectMapper.writeValueAsString(message.getPayload());
                Message rabbitMessage = new Message(jsonPayload.getBytes(), messageProperties);
                String routingKey = StringUtils.hasText(message.getTag()) ? message.getTag() : "";

                transactionTemplate.send(message.getTopic(), routingKey, rabbitMessage);
                return true;
            } else {
                // 回滚事务
                log.info("事务回滚，不发送消息: transactionId={}", transactionId);
                return false;
            }

        } catch (Exception e) {
            log.error("RabbitMQ发送事务消息失败", e);
            return false;
        }
    }

    /**
     * 创建顺序队列
     */
    private void ensureOrderQueue(String exchangeName, String queueName) {
        // 创建直连交换机（保证顺序）
        Exchange exchange = new DirectExchange(exchangeName, true, false);
        amqpAdmin.declareExchange(exchange);

        // 创建队列
        Queue queue = new Queue(queueName, true, false, false);
        amqpAdmin.declareQueue(queue);

        // 绑定队列到交换机
        Binding binding = BindingBuilder.bind(queue).to(exchange).with("").noargs();
        amqpAdmin.declareBinding(binding);
    }

    // ==================== AdvancedMqProducer 接口实现 ====================

    @Override
    public boolean supportsAsync() {
        return true;
    }

    @Override
    public boolean supportsOneWay() {
        return true; // 通过不等待确认模拟
    }

    @Override
    public boolean supportsOrderly() {
        return true; // 通过单队列实现
    }

    @Override
    public boolean supportsTransaction() {
        return properties.getProducer().isTransactionEnabled();
    }

    @Override
    public boolean supportsDelay() {
        return true; // 需要rabbitmq-delayed-message-exchange插件
    }

    @Override
    public long[] getSupportedDelayLevels() {
        // RabbitMQ支持任意延迟时间（毫秒级）
        return new long[]{
                1000L,      // 1s
                5000L,      // 5s
                10000L,     // 10s
                30000L,     // 30s
                60000L,     // 1m
                300000L,    // 5m
                600000L,    // 10m
                1800000L,   // 30m
                3600000L,   // 1h
                7200000L    // 2h
        };
    }

    @Override
    public int getMaxBatchSize() {
        return 500; // RabbitMQ建议的批量大小
    }
}
