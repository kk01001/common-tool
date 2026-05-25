package io.github.archer099.gateway;

import io.github.archer099.gateway.config.GrayLoadBalancerConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;

/**
 * @author archer099
 * @date 2025-01-08 17:00:00
 * @description 网关启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
@LoadBalancerClients(defaultConfiguration = GrayLoadBalancerConfig.class)
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}