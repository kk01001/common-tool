package io.github.archer099;

import cn.hutool.core.net.Ipv4Util;
import io.github.archer099.core.ApplicationInfoInitialize;
import io.github.archer099.id.IdWorkerUtil;
import io.github.archer099.util.NetworkUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

/**
 * @author archer099
 * date:  2024-07-02 15:49
 */
@Slf4j
@ComponentScan(basePackages = "io.github.archer099")
@Configuration(proxyBeanMethods = false)
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class CommonToolConfiguration {

    @Bean
    public ApplicationInfoInitialize applicationInfoInitialize() {
        return new ApplicationInfoInitialize();
    }

    @Bean
    public IdWorkerUtil idWorkerUtil() {
        String localIp = NetworkUtil.LOCAL_SERVER_IP;
        long value = Ipv4Util.ipv4ToLong(localIp);
        long workerId = value % 1024;
        log.info("init Id Worker, localIp: {}, workerId: {}", localIp, workerId);
        return new IdWorkerUtil(workerId);
    }
}
