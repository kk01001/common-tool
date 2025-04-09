package io.github.kk01001.design.pattern.statemachine.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * @author kk01001
 * @date 2024-04-07 14:31:00
 * @description 状态机配置属性
 */
@Data
@ConfigurationProperties(prefix = "state-machine")
public class StateMachineProperties {

    /**
     * 是否启用状态机历史记录
     */
    private Boolean historyEnabled = false;

    private Metrics metrics = new Metrics();

    private Persister persister = new Persister();

    @Data
    public static class Metrics {

        private Boolean enabled = true;
    }

    @Data
    public static class Persister {

        /**
         * 持久化类型：memory, redisson
         */
        private String type = "memory";

        /**
         * Redis key 前缀
         */
        private String keyPrefix = "statemachine:state";

        private Duration timeout = Duration.ofSeconds(30);
    }
}