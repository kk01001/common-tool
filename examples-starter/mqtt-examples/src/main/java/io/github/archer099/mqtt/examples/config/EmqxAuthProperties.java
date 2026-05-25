package io.github.archer099.mqtt.examples.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author archer099
 * @date 2025-11-17 15:45:00
 * @description EMQX HTTP 鉴权属性配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "emqx.auth")
public class EmqxAuthProperties {

    private List<User> users;

    @Data
    public static class User {
        private String username;
        private String password;
        private Boolean superuser = false;
    }
}