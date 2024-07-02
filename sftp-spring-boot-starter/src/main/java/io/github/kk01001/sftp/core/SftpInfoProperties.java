package io.github.kk01001.sftp.core;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * @author kk01001
 * date:  2023-03-28 16:01
 */
@Data
@ConfigurationProperties(prefix = "sftp-pool")
public class SftpInfoProperties {

    private Boolean enable;

    private String host;

    private Integer port = 22;

    private String username;

    private String password;

    private Duration connectTimeout = Duration.ofSeconds(3);

    private Duration minEvictableIdleTime = Duration.ofSeconds(5);

    private Integer maxIdle;

    private Integer minIdle;

    private Integer maxTotal;

}
