package io.github.kk01001.sftp;

import io.github.kk01001.sftp.core.JschConnectionPool;
import io.github.kk01001.sftp.core.SftpInfoProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author kk01001
 * date:  2024-07-01 16:14
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(SftpInfoProperties.class)
@ConditionalOnProperty(prefix = "sftp-pool", name = "enable", havingValue = "true")
public class SftpConfiguration {

    private final SftpInfoProperties sftpInfoProperties;

    @Bean
    public JschConnectionPool defaultConnectionPool() {
        return new JschConnectionPool();
    }

    @Bean
    public JschConnectionPool jschConnectionPool() {
        JschConnectionPool pool = new JschConnectionPool(sftpInfoProperties.getHost(),
                sftpInfoProperties.getPort(),
                sftpInfoProperties.getUsername(),
                sftpInfoProperties.getPassword(),
                sftpInfoProperties.getMaxTotal(),
                sftpInfoProperties.getMinIdle(),
                sftpInfoProperties.getMaxIdle(),
                sftpInfoProperties.getConnectTimeout(),
                sftpInfoProperties.getMinEvictableIdleTime());
        log.info("sftp连接池初始化完成");
        return pool;
    }
}
