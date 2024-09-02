package io.github.kk01001.mq.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author kk01001
 * @date 2024-08-23 09:27:00
 * @description
 */
@Data
@Component
@ConfigurationProperties(prefix = "dynamic-mq")
public class DynamicMqProperties {

    /**
     * 是否启用
     */
    private boolean enable = false;

    /**
     * MQ实现：kafka|rocket|redis|rabbit，一个应用只用一套MQ发布和订阅
     */
    private String type = "none";

    /**
     * 服务地址
     */
    private String server = "";

    /**
     * 用户
     */
    private String username = "";

    /**
     * 密码
     */
    private String password = "";


    /**
     * 命名空间，或作为所有主题的前缀，用于环境隔离等场景，如UAT/生产/租户环境共用一个MQ
     */
    private String namespace = "";

    /**
     * 是否持久化到本地，需要定义实现了MQEventStorer的Bean
     */
    private boolean persist = false;

    /**
     * 序列化器，定义实现了MQEventSerialization的Bean
     */
    private String serialization = "JsonMQEventSerialization";

    /**
     * 是否自动提交
     */
    private boolean autoAck = false;

    /**
     * 发送失败重试次数
     */
    private int retries = 0;

    /**
     * 生产者组
     */
    private String producerGroup = "DEFAULT";

    /**
     * 默认主题
     */
    private String defaultTopic = "DEFAULT";

    /**
     * 交换机
     */
    private String exchange = "";
}
