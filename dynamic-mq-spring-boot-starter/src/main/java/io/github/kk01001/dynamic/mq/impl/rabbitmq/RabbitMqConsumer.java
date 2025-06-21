package io.github.kk01001.dynamic.mq.impl.rabbitmq;

import io.github.kk01001.dynamic.mq.config.DynamicMqProperties;
import io.github.kk01001.dynamic.mq.core.AbstractMqConsumer;
import io.github.kk01001.dynamic.mq.core.MqDeadLetterHandler;
import io.github.kk01001.dynamic.mq.core.MqMessageFilter;
import io.github.kk01001.dynamic.mq.core.MqRetryStrategy;
import io.github.kk01001.dynamic.mq.enums.ConsumeMode;
import io.github.kk01001.dynamic.mq.enums.MessageModel;
import io.github.kk01001.dynamic.mq.model.MqMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author linshiqiang
 * @date 2025-06-21 20:48:56
 * @description RabbitMQ消费者实现
 */
@Slf4j
@RequiredArgsConstructor
public class RabbitMqConsumer extends AbstractMqConsumer {
    
    private final DynamicMqProperties properties;
    
    private ConnectionFactory connectionFactory;
    private AmqpAdmin amqpAdmin;
    private RabbitTemplate rabbitTemplate;
    private final Map<String, SimpleMessageListenerContainer> containers = new ConcurrentHashMap<>();
    private final Map<String, MqMessageHandler> messageHandlers = new ConcurrentHashMap<>();

    // 高级功能组件
    private MqRetryStrategy retryStrategy;
    private MqDeadLetterHandler deadLetterHandler;
    private MqMessageFilter messageFilter;
    
    @Override
    protected void doStart() {
        try {
            initConnectionFactory();
            initAmqpAdmin();
            initRabbitTemplate();
            initAdvancedFeatures();
            containers.values().forEach(SimpleMessageListenerContainer::start);
            log.info("RabbitMQ消费者已启动");
        } catch (Exception e) {
            log.error("RabbitMQ消费者启动失败", e);
            throw new RuntimeException("RabbitMQ消费者启动失败", e);
        }
    }
    
    @Override
    protected void doStop() {
        containers.values().forEach(SimpleMessageListenerContainer::stop);
        containers.clear();
        messageHandlers.clear();
        
        if (connectionFactory instanceof CachingConnectionFactory) {
            ((CachingConnectionFactory) connectionFactory).destroy();
        }
        log.info("RabbitMQ消费者已停止");
    }
    
    @Override
    protected void doSubscribe(String topic, String tag, MqMessageHandler messageHandler) {
        try {
            String queueName = buildQueueName(topic, tag);
            String key = topic + ":" + (StringUtils.hasText(tag) ? tag : "");
            
            // 确保交换机和队列存在
            ensureExchangeAndQueue(topic, tag);
            
            // 创建消息监听容器
            SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
            container.setConnectionFactory(connectionFactory);
            container.setQueueNames(queueName);
            container.setConcurrentConsumers(properties.getConsumer().getConsumeThreadMin());
            container.setMaxConcurrentConsumers(properties.getConsumer().getConsumeThreadMax());
            
            // 设置消息监听器
            container.setMessageListener(new ChannelAwareMessageListener() {
                @Override
                public void onMessage(Message message, com.rabbitmq.client.Channel channel) throws Exception {
                    try {
                        MqMessage mqMessage = convertToMqMessage(message);
                        boolean success = messageHandler.handle(mqMessage);
                        
                        if (success) {
                            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                        } else {
                            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
                        }
                    } catch (Exception e) {
                        log.error("处理RabbitMQ消息失败", e);
                        channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
                    }
                }
            });
            
            if (running) {
                container.start();
            }
            
            containers.put(key, container);
            messageHandlers.put(key, messageHandler);
            
            log.info("RabbitMQ订阅成功: topic={}, tag={}, queue={}", topic, tag, queueName);
        } catch (Exception e) {
            log.error("RabbitMQ订阅失败: topic={}, tag={}", topic, tag, e);
            throw new RuntimeException("RabbitMQ订阅失败", e);
        }
    }
    
    @Override
    protected void doUnsubscribe(String topic) {
        containers.entrySet().removeIf(entry -> {
            if (entry.getKey().startsWith(topic + ":")) {
                entry.getValue().stop();
                return true;
            }
            return false;
        });
        messageHandlers.entrySet().removeIf(entry -> entry.getKey().startsWith(topic + ":"));
        log.info("RabbitMQ取消订阅: topic={}", topic);
    }
    
    @Override
    public String getConsumerType() {
        return "rabbitmq";
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
    
    private void initAmqpAdmin() {
        this.amqpAdmin = new RabbitAdmin(connectionFactory);
    }

    private void initRabbitTemplate() {
        this.rabbitTemplate = new RabbitTemplate(connectionFactory);
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
    
    private MqMessage convertToMqMessage(Message message) throws Exception {
        String payload = new String(message.getBody());

        return MqMessage.builder()
                .messageId(message.getMessageProperties().getMessageId())
                .topic(message.getMessageProperties().getReceivedExchange())
                .tag(message.getMessageProperties().getReceivedRoutingKey())
                .payload(payload)
                .createTime(System.currentTimeMillis())
                .build();
    }

    /**
     * 初始化高级功能组件
     */
    private void initAdvancedFeatures() {
        // 初始化重试策略
        if (properties.getConsumer().getRetry().isEnabled()) {
            String strategy = properties.getConsumer().getRetry().getStrategy();
            switch (strategy) {
                case "fixed":
                    this.retryStrategy = MqRetryStrategy.fixedDelay(
                            properties.getConsumer().getRetry().getMaxRetryTimes(),
                            properties.getConsumer().getRetry().getFixedDelay()
                    );
                    break;
                case "exponential":
                    this.retryStrategy = MqRetryStrategy.exponentialBackoff(
                            properties.getConsumer().getRetry().getMaxRetryTimes(),
                            properties.getConsumer().getRetry().getInitialDelay(),
                            properties.getConsumer().getRetry().getMultiplier()
                    );
                    break;
                default:
                    this.retryStrategy = MqRetryStrategy.fixedDelay(3, 1000L);
            }
        }

        // 初始化死信队列处理器
        if (properties.getConsumer().getDeadLetter().isEnabled()) {
            String handler = properties.getConsumer().getDeadLetter().getHandler();
            switch (handler) {
                case "logging":
                    this.deadLetterHandler = MqDeadLetterHandler.loggingHandler();
                    break;
                case "default":
                default:
                    this.deadLetterHandler = MqDeadLetterHandler.defaultHandler();
            }
        }

        // 初始化消息过滤器
        if (properties.getConsumer().getFilter().isEnabled()) {
            String filterType = properties.getConsumer().getFilter().getDefaultType();
            switch (filterType) {
                case "tag":
                    this.messageFilter = MqMessageFilter.tagFilter("*");
                    break;
                case "sql":
                    this.messageFilter = MqMessageFilter.sqlFilter("1=1");
                    break;
                default:
                    this.messageFilter = MqMessageFilter.tagFilter("*");
            }
        }
    }

    /**
     * 增强的消息处理（支持重试、死信队列、过滤等）
     */
    private void handleMessageWithAdvancedFeatures(MqMessage mqMessage, MqMessageHandler messageHandler,
                                                   com.rabbitmq.client.Channel channel, Message message) throws Exception {
        // 消息过滤
        if (messageFilter != null && !messageFilter.filter(mqMessage)) {
            log.debug("消息被过滤: {}", mqMessage.getMessageId());
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }

        int retryCount = getRetryCount(message);
        boolean success = false;
        Exception lastException = null;

        try {
            success = messageHandler.handle(mqMessage);
        } catch (Exception e) {
            lastException = e;
            log.error("处理消息失败: messageId={}, retryCount={}", mqMessage.getMessageId(), retryCount, e);
        }

        if (success) {
            // 消息处理成功
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } else {
            // 消息处理失败，判断是否需要重试
            if (retryStrategy != null && retryStrategy.shouldRetry(mqMessage, retryCount, lastException)) {
                // 重试：发送到延迟队列
                long delay = retryStrategy.getRetryDelay(retryCount + 1);
                sendToRetryQueue(mqMessage, retryCount + 1, delay);
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } else {
                // 达到最大重试次数，发送到死信队列
                if (deadLetterHandler != null) {
                    deadLetterHandler.handleDeadLetter(mqMessage, mqMessage.getTopic(),
                            properties.getConsumer().getGroupName(), retryCount, lastException);
                }
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            }
        }
    }

    /**
     * 获取消息重试次数
     */
    private int getRetryCount(Message message) {
        Object retryCount = message.getMessageProperties().getHeaders().get("x-retry-count");
        return retryCount != null ? (Integer) retryCount : 0;
    }

    /**
     * 发送消息到重试队列
     */
    private void sendToRetryQueue(MqMessage mqMessage, int retryCount, long delay) {
        try {
            // 创建重试队列
            String retryQueueName = mqMessage.getTopic() + ".retry";
            ensureRetryQueue(retryQueueName, mqMessage.getTopic(), delay);

            // 构建重试消息
            MessageProperties messageProperties = new MessageProperties();
            messageProperties.setContentType("application/json");
            messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            // 使用消息头设置延迟时间，替代过时的setDelay方法
            messageProperties.setHeader("x-delay", (int) delay);
            messageProperties.getHeaders().put("x-retry-count", retryCount);

            String jsonPayload = new com.fasterxml.jackson.databind.ObjectMapper()
                    .writeValueAsString(mqMessage.getPayload());
            Message retryMessage = new Message(jsonPayload.getBytes(), messageProperties);

            // 发送到重试队列
            rabbitTemplate.send(retryQueueName, "", retryMessage);

            log.info("消息发送到重试队列: messageId={}, retryCount={}, delay={}ms",
                    mqMessage.getMessageId(), retryCount, delay);

        } catch (Exception e) {
            log.error("发送消息到重试队列失败", e);
        }
    }

    /**
     * 确保重试队列存在
     */
    private void ensureRetryQueue(String retryQueueName, String originalTopic, long delay) {
        // 创建延迟交换机
        Map<String, Object> args = new java.util.HashMap<>();
        args.put("x-delayed-type", "direct");
        Exchange delayExchange = new CustomExchange(retryQueueName + ".delay", "x-delayed-message", true, false, args);
        amqpAdmin.declareExchange(delayExchange);

        // 创建重试队列
        Queue retryQueue = new Queue(retryQueueName, true, false, false);
        amqpAdmin.declareQueue(retryQueue);

        // 绑定重试队列到延迟交换机
        Binding retryBinding = BindingBuilder.bind(retryQueue).to(delayExchange).with("").noargs();
        amqpAdmin.declareBinding(retryBinding);

        // 绑定重试队列到原始交换机（延迟后重新投递）
        Exchange originalExchange = new TopicExchange(originalTopic, true, false);
        Binding originalBinding = BindingBuilder.bind(retryQueue).to(originalExchange).with("#").noargs();
        amqpAdmin.declareBinding(originalBinding);
    }
}
