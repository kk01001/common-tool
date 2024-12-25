package io.github.kk01001.push;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author linshiqiang
 * @date 2024-12-25 14:43:00
 * @description
 */
@ComponentScan(basePackages = "io.github.kk01001.push")
@Configuration(proxyBeanMethods = false)
public class MessagePushConfiguration {

}
