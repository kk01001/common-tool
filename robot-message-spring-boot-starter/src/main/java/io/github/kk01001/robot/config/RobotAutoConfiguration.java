package io.github.kk01001.robot.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 机器人自动配置类
 * 负责创建和配置机器人相关的Bean
 */
@ComponentScan(basePackages = "io.github.kk01001.robot")
@Configuration
@EnableConfigurationProperties({
    RobotProperties.class,
    SmsScriptProperties.class
})
public class RobotAutoConfiguration {

} 