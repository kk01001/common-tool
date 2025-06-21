package io.github.kk01001.dynamic.mq.impl.rocketmq;

import io.github.kk01001.dynamic.mq.config.DynamicMqProperties;
import io.github.kk01001.dynamic.mq.core.AbstractMqConsumer;
import io.github.kk01001.dynamic.mq.model.MqMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.*;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.protocol.heartbeat.MessageModel;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author linshiqiang
 * @date 2025-06-21 20:48:56
 * @description RocketMQ消费者实现
 */
@Slf4j
@RequiredArgsConstructor
public class RocketMqConsumer extends AbstractMqConsumer {
    
    private final DynamicMqProperties properties;
    private final Map<String, DefaultMQPushConsumer> consumers = new ConcurrentHashMap<>();
    private final Map<String, MqMessageHandler> messageHandlers = new ConcurrentHashMap<>();
    
    @Override
    protected void doStart() {
        log.info("RocketMQ消费者已启动");
    }
    
    @Override
    protected void doStop() {
        consumers.values().forEach(DefaultMQPushConsumer::shutdown);
        consumers.clear();
        messageHandlers.clear();
        log.info("RocketMQ消费者已停止");
    }
    
    @Override
    protected void doSubscribe(String topic, String tag, MqMessageHandler messageHandler) {
        try {
            String key = topic + ":" + (StringUtils.hasText(tag) ? tag : "*");

            DefaultMQPushConsumer consumer = createConsumer(key);
            consumer.subscribe(topic, StringUtils.hasText(tag) ? tag : "*");

            // 根据配置选择消费模式
            registerMessageListener(consumer, messageHandler);

            consumer.start();
            consumers.put(key, consumer);
            messageHandlers.put(key, messageHandler);

            log.info("RocketMQ订阅成功: topic={}, tag={}, 消费模式={}, 消息模型={}",
                    topic, tag, getConsumeMode(), getMessageModel());
        } catch (Exception e) {
            log.error("RocketMQ订阅失败: topic={}, tag={}", topic, tag, e);
            throw new RuntimeException("RocketMQ订阅失败", e);
        }
    }
    
    @Override
    protected void doUnsubscribe(String topic) {
        consumers.entrySet().removeIf(entry -> {
            if (entry.getKey().startsWith(topic + ":")) {
                entry.getValue().shutdown();
                return true;
            }
            return false;
        });
        messageHandlers.entrySet().removeIf(entry -> entry.getKey().startsWith(topic + ":"));
        log.info("RocketMQ取消订阅: topic={}", topic);
    }
    
    @Override
    public String getConsumerType() {
        return "rocketmq";
    }
    
    private MqMessage convertToMqMessage(MessageExt messageExt) {
        return MqMessage.builder()
                .messageId(messageExt.getMsgId())
                .topic(messageExt.getTopic())
                .tag(messageExt.getTags())
                .payload(new String(messageExt.getBody()))
                .createTime(messageExt.getBornTimestamp())
                .build();
    }

    /**
     * 创建消费者
     */
    private DefaultMQPushConsumer createConsumer(String key) {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer();
        consumer.setConsumerGroup(properties.getConsumer().getGroupName());
        consumer.setNamesrvAddr(properties.getRocketmq().getNameServer());
        consumer.setConsumeThreadMin(properties.getConsumer().getConsumeThreadMin());
        consumer.setConsumeThreadMax(properties.getConsumer().getConsumeThreadMax());
        consumer.setConsumeMessageBatchMaxSize(properties.getConsumer().getConsumeMessageBatchMaxSize());

        // 设置消息模型（集群或广播）
        if (getMessageModel() == io.github.kk01001.dynamic.mq.enums.MessageModel.BROADCASTING) {
            consumer.setMessageModel(MessageModel.BROADCASTING);
        } else {
            consumer.setMessageModel(MessageModel.CLUSTERING);
        }

        return consumer;
    }

    /**
     * 注册消息监听器
     */
    private void registerMessageListener(DefaultMQPushConsumer consumer, MqMessageHandler messageHandler) {
        if (getConsumeMode() == io.github.kk01001.dynamic.mq.enums.ConsumeMode.ORDERLY) {
            // 顺序消费
            consumer.registerMessageListener(new MessageListenerOrderly() {
                @Override
                public ConsumeOrderlyStatus consumeMessage(
                        List<MessageExt> messages,
                        ConsumeOrderlyContext context) {

                    for (MessageExt messageExt : messages) {
                        try {
                            MqMessage mqMessage = convertToMqMessage(messageExt);
                            boolean success = messageHandler.handle(mqMessage);
                            if (!success) {
                                return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
                            }
                        } catch (Exception e) {
                            log.error("顺序处理RocketMQ消息失败", e);
                            return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
                        }
                    }
                    return ConsumeOrderlyStatus.SUCCESS;
                }
            });
        } else {
            // 并发消费
            consumer.registerMessageListener(new MessageListenerConcurrently() {
                @Override
                public ConsumeConcurrentlyStatus consumeMessage(
                        List<MessageExt> messages,
                        ConsumeConcurrentlyContext context) {

                    for (MessageExt messageExt : messages) {
                        try {
                            MqMessage mqMessage = convertToMqMessage(messageExt);
                            boolean success = messageHandler.handle(mqMessage);
                            if (!success) {
                                return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                            }
                        } catch (Exception e) {
                            log.error("并发处理RocketMQ消息失败", e);
                            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                        }
                    }
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
            });
        }
    }

    /**
     * 获取消费模式
     */
    private io.github.kk01001.dynamic.mq.enums.ConsumeMode getConsumeMode() {
        // 从配置中获取，如果没有配置则默认为并发消费
        return properties.getConsumer().getConsumeMode() != null ?
                properties.getConsumer().getConsumeMode() :
                io.github.kk01001.dynamic.mq.enums.ConsumeMode.CONCURRENTLY;
    }

    /**
     * 获取消息模型
     */
    private io.github.kk01001.dynamic.mq.enums.MessageModel getMessageModel() {
        // 从配置中获取，如果没有配置则默认为集群模式
        return properties.getConsumer().getMessageModel() != null ?
                properties.getConsumer().getMessageModel() :
                io.github.kk01001.dynamic.mq.enums.MessageModel.CLUSTERING;
    }
}
