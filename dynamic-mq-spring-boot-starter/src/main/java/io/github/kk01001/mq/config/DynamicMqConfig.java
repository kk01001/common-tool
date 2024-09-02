package io.github.kk01001.mq.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.annotation.Order;

/**
 * @author kk01001
 * @date 2024-08-23 09:24:00
 * @description
 */
@Order(PriorityOrdered.HIGHEST_PRECEDENCE + 10)
@Configuration
@ConditionalOnProperty(prefix = "dynamic-mq", name = "enable", havingValue = "true")
public class DynamicMqConfig {
}
