package com.github.kk0.xxljob.config;

import com.github.kk0.util.Utils;
import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

/**
 * @author linshiqiang
 * @date 2022/2/14 10:34
 */
@Slf4j
@ComponentScan(basePackages = "com.github.kk0")
@Configuration
@ConditionalOnProperty(prefix = "xxl-job", name = "enable", havingValue = "true")
public class XxlJobConfiguration {


    private final XxlJobProperties xxlJobProperties;

    public XxlJobConfiguration(XxlJobProperties xxlJobProperties) {
        this.xxlJobProperties = xxlJobProperties;
    }

    @Bean
    public XxlJobSpringExecutor xxlJobExecutor() {
        Assert.notNull(xxlJobProperties.getAdminAddresses(), "xxl-job adminAddresses is null");
        Assert.notNull(xxlJobProperties.getAppName(), "xxl-job appName is null");
        Assert.notNull(xxlJobProperties.getLogPath(), "xxl-job LogPath is null");
        Assert.notNull(xxlJobProperties.getLogRetentionDays(), "xxl-job logRetentionDays is null");

        String ipAddress = Utils.getLocalServerIp();
        log.info(">>>>>>>>>>> xxl-job config init. {}", ipAddress);
        XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
        xxlJobSpringExecutor.setAdminAddresses(xxlJobProperties.getAdminAddresses());
        xxlJobSpringExecutor.setAppname(xxlJobProperties.getAppName());
        xxlJobSpringExecutor.setIp(ipAddress);
        xxlJobSpringExecutor.setPort(xxlJobProperties.getPort());
        xxlJobSpringExecutor.setAccessToken(xxlJobProperties.getAccessToken());
        xxlJobSpringExecutor.setLogPath(xxlJobProperties.getLogPath());
        xxlJobSpringExecutor.setLogRetentionDays(xxlJobProperties.getLogRetentionDays());
        return xxlJobSpringExecutor;
    }

}
