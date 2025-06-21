package io.github.kk01001.dynamic.mq.impl.rocketmq;

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
import org.apache.rocketmq.client.producer.*;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;

import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * @author linshiqiang
 * @date 2025-06-21 20:48:56
 * @description RocketMQ生产者实现
 */
@Slf4j
@RequiredArgsConstructor
public class RocketMqProducer extends AbstractMqProducer implements AdvancedMqProducer {
    
    private final DynamicMqProperties properties;
    private DefaultMQProducer producer;
    private TransactionMQProducer transactionProducer;
    
    @PostConstruct
    public void init() {
        try {
            initProducer();
            initTransactionProducer();
            log.info("RocketMQ生产者初始化成功");
        } catch (Exception e) {
            log.error("RocketMQ生产者初始化失败", e);
            throw new RuntimeException("RocketMQ生产者初始化失败", e);
        }
    }
    
    @PreDestroy
    public void destroy() {
        try {
            if (producer != null) {
                producer.shutdown();
            }
            if (transactionProducer != null) {
                transactionProducer.shutdown();
            }
            log.info("RocketMQ生产者资源清理完成");
        } catch (Exception e) {
            log.error("RocketMQ生产者资源清理失败", e);
        }
    }
    
    private void initProducer() throws Exception {
        producer = new DefaultMQProducer();
        producer.setProducerGroup(properties.getRocketmq().getProducerGroup());
        producer.setNamesrvAddr(properties.getRocketmq().getNameServer());
        producer.setSendMsgTimeout((int) properties.getProducer().getSendTimeout());
        producer.setRetryTimesWhenSendFailed(properties.getProducer().getMaxRetryTimes());
        producer.start();
    }
    
    @Override
    protected boolean doSend(MqMessage message) {
        try {
            Message rocketMessage = buildRocketMessage(message);
            SendResult sendResult = producer.send(rocketMessage);
            return sendResult != null;
        } catch (Exception e) {
            log.error("RocketMQ发送消息失败", e);
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
            log.error("RocketMQ批量发送消息失败", e);
            return false;
        }
    }
    

    
    @Override
    public String getProducerType() {
        return "rocketmq";
    }
    
    private Message buildRocketMessage(MqMessage message) {
        Message rocketMessage = new Message();
        rocketMessage.setTopic(message.getTopic());
        rocketMessage.setTags(message.getTag());
        rocketMessage.setBody(message.getPayload().toString().getBytes());
        
        if (message.getDelayTime() != null && message.getDelayTime() > 0) {
            // RocketMQ延迟消息级别设置
            rocketMessage.setDelayTimeLevel(calculateDelayLevel(message.getDelayTime()));
        }
        
        return rocketMessage;
    }
    
    private int calculateDelayLevel(long delayTime) {
        // RocketMQ延迟级别：1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h
        long seconds = delayTime / 1000;
        if (seconds <= 1) return 1;
        if (seconds <= 5) return 2;
        if (seconds <= 10) return 3;
        if (seconds <= 30) return 4;
        if (seconds <= 60) return 5;
        if (seconds <= 120) return 6;
        if (seconds <= 180) return 7;
        if (seconds <= 240) return 8;
        if (seconds <= 300) return 9;
        if (seconds <= 360) return 10;
        if (seconds <= 420) return 11;
        if (seconds <= 480) return 12;
        if (seconds <= 540) return 13;
        if (seconds <= 600) return 14;
        if (seconds <= 1200) return 15;
        if (seconds <= 1800) return 16;
        if (seconds <= 3600) return 17;
        return 18; // 2h
    }

    /**
     * 转换RocketMQ消息为通用消息
     */
    private MqMessage convertToMqMessage(Message msg) {
        return MqMessage.builder()
                .topic(msg.getTopic())
                .tag(msg.getTags())
                .payload(new String(msg.getBody()))
                .createTime(System.currentTimeMillis())
                .build();
    }

    /**
     * 转换通用事务状态为RocketMQ事务状态
     */
    private LocalTransactionState convertToRocketMqState(TransactionState state) {
        switch (state) {
            case COMMIT:
                return LocalTransactionState.COMMIT_MESSAGE;
            case ROLLBACK:
                return LocalTransactionState.ROLLBACK_MESSAGE;
            case UNKNOWN:
            default:
                return LocalTransactionState.UNKNOW;
        }
    }

    /**
     * 初始化事务生产者
     */
    private void initTransactionProducer() throws Exception {
        if (!properties.getProducer().isTransactionEnabled()) {
            return;
        }

        transactionProducer = new TransactionMQProducer();
        transactionProducer.setProducerGroup(properties.getRocketmq().getProducerGroup() + "_transaction");
        transactionProducer.setNamesrvAddr(properties.getRocketmq().getNameServer());
        transactionProducer.setSendMsgTimeout((int) properties.getProducer().getSendTimeout());

        // 设置默认事务监听器（实际的监听器会在发送时动态设置）
        transactionProducer.setTransactionListener(new TransactionListener() {
            @Override
            public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
                if (arg instanceof MqTransactionListener) {
                    MqTransactionListener listener = (MqTransactionListener) arg;
                    MqMessage mqMessage = convertToMqMessage(msg);
                    TransactionState state = listener.executeLocalTransaction(mqMessage, "default");
                    return convertToRocketMqState(state);
                }
                log.warn("未提供事务监听器，默认回滚事务");
                return LocalTransactionState.ROLLBACK_MESSAGE;
            }

            @Override
            public LocalTransactionState checkLocalTransaction(MessageExt msg) {
                // 这里需要根据消息找到对应的事务监听器
                // 简化实现：默认提交
                log.warn("检查本地事务状态，默认提交: {}", new String(msg.getBody()));
                return LocalTransactionState.COMMIT_MESSAGE;
            }
        });

        transactionProducer.start();
    }

    @Override
    public void sendAsync(MqMessage message, MqSendCallback callback) {
        try {
            Message rocketMessage = buildRocketMessage(message);
            producer.send(rocketMessage, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    MqSendResult result = MqSendResult.builder()
                            .messageId(sendResult.getMsgId())
                            .topic(message.getTopic())
                            .tag(message.getTag())
                            .status("SUCCESS")
                            .queueId(String.valueOf(sendResult.getMessageQueue().getQueueId()))
                            .queueOffset(sendResult.getQueueOffset())
                            .sendTimestamp(System.currentTimeMillis())
                            .build();
                    callback.onSuccess(result);
                }

                @Override
                public void onException(Throwable e) {
                    callback.onException(e);
                }
            });
        } catch (Exception e) {
            log.error("RocketMQ发送异步消息失败", e);
            callback.onException(e);
        }
    }

    @Override
    public void sendOneWay(MqMessage message) {
        try {
            Message rocketMessage = buildRocketMessage(message);
            producer.sendOneway(rocketMessage);
        } catch (Exception e) {
            log.error("RocketMQ发送OneWay消息失败", e);
        }
    }

    @Override
    public boolean sendOrderly(MqMessage message, String orderKey) {
        try {
            Message rocketMessage = buildRocketMessage(message);

            // 使用orderKey选择队列，保证相同orderKey的消息发送到同一个队列
            SendResult result = producer.send(rocketMessage, new MessageQueueSelector() {
                @Override
                public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
                    String key = (String) arg;
                    int index = Math.abs(key.hashCode()) % mqs.size();
                    return mqs.get(index);
                }
            }, orderKey);

            return result != null;
        } catch (Exception e) {
            log.error("RocketMQ发送顺序消息失败", e);
            return false;
        }
    }

    @Override
    public boolean sendTransaction(MqMessage message, String transactionId, MqTransactionListener transactionListener) {
        try {
            if (transactionProducer == null) {
                log.error("事务生产者未初始化");
                return false;
            }

            Message rocketMessage = buildRocketMessage(message);
            TransactionSendResult result = transactionProducer.sendMessageInTransaction(
                    rocketMessage, transactionListener);

            return result.getLocalTransactionState() == LocalTransactionState.COMMIT_MESSAGE;
        } catch (Exception e) {
            log.error("RocketMQ发送事务消息失败", e);
            return false;
        }
    }

    // ==================== AdvancedMqProducer 接口实现 ====================

    @Override
    public boolean supportsAsync() {
        return true;
    }

    @Override
    public boolean supportsOneWay() {
        return true;
    }

    @Override
    public boolean supportsOrderly() {
        return true;
    }

    @Override
    public boolean supportsTransaction() {
        return properties.getProducer().isTransactionEnabled();
    }

    @Override
    public boolean supportsDelay() {
        return true;
    }

    @Override
    public long[] getSupportedDelayLevels() {
        // RocketMQ支持的18个延迟级别（毫秒）
        return new long[]{
                1000L,      // 1s
                5000L,      // 5s
                10000L,     // 10s
                30000L,     // 30s
                60000L,     // 1m
                120000L,    // 2m
                180000L,    // 3m
                240000L,    // 4m
                300000L,    // 5m
                360000L,    // 6m
                420000L,    // 7m
                480000L,    // 8m
                540000L,    // 9m
                600000L,    // 10m
                1200000L,   // 20m
                1800000L,   // 30m
                3600000L,   // 1h
                7200000L    // 2h
        };
    }

    @Override
    public int getMaxBatchSize() {
        return 1000; // RocketMQ建议的最大批量大小
    }
}
