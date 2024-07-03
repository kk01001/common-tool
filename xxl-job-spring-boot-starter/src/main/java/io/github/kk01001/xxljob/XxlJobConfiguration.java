package io.github.kk01001.xxljob;

import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import io.github.kk01001.xxljob.core.XxlJobProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

/**
 * @author kk01001
 * @date 2022/2/14 10:34
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@ComponentScan(basePackages = "io.github.kk01001")
@EnableConfigurationProperties(XxlJobProperties.class)
@ConditionalOnProperty(prefix = "xxl-job", name = "enable", havingValue = "true")
public class XxlJobConfiguration {

    private static final String SERVER_IP_KEY = "SERVER_IP";

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
        Assert.notNull(xxlJobProperties.getPort(), "xxl-job port is null");

        String ipAddress = getLocalServerIp();
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

    public static String getLocalServerIp() {
        String serverIp = SpringUtil.getProperty(SERVER_IP_KEY);
        if (StrUtil.isNotEmpty(serverIp)) {
            return serverIp;
        }
        return NetUtil.getLocalhostStr();
    }

}
