package io.github.kk01001;

import cn.hutool.core.net.Ipv4Util;
import io.github.kk01001.core.ApplicationInfoInitialize;
import io.github.kk01001.id.IdWorkerUtil;
import io.github.kk01001.util.NetworkUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author kk01001
 * date:  2024-07-02 15:49
 */
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
        return new IdWorkerUtil(workerId);
    }

}
