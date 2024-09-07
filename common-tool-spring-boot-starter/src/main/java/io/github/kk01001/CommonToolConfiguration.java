package io.github.kk01001;

import cn.hutool.core.net.Ipv4Util;
import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import io.github.kk01001.core.ApplicationInfoInitialize;
import io.github.kk01001.id.IdWorkerUtil;
import io.github.kk01001.util.NetworkUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * @author kk01001
 * date:  2024-07-02 15:49
 */
@Slf4j
@ComponentScan(basePackages = "io.github.kk01001")
@Configuration(proxyBeanMethods = false)
public class CommonToolConfiguration {

    @Bean
    public ApplicationInfoInitialize applicationInfoInitialize() {
        return new ApplicationInfoInitialize();
    }

    @Bean
    public IdWorkerUtil idWorkerUtil() {
        String localIp = NetworkUtil.getLocalIp();
        long value = Ipv4Util.ipv4ToLong(localIp);
        long workerId = value % 1024;
        log.info("init Id Worker, localIp: {}, workerId: {}", localIp, workerId);
        return new IdWorkerUtil(workerId);
    }

    @ConditionalOnMissingBean(ConfigService.class)
    @ConditionalOnBean(NacosConfigProperties.class)
    @ConditionalOnClass(NacosConfigProperties.class)
    @Bean
    public ConfigService configService(NacosConfigProperties nacosConfigProperties) throws NacosException {
        log.info("init nacos config service");
        Properties properties = nacosConfigProperties.assembleConfigServiceProperties();
        return NacosFactory.createConfigService(properties);
    }

}
