package io.github.kk01001.design.pattern.statemachine.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

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

    @Data
    public static class Metrics {

        private Boolean enabled = true;
    }
}