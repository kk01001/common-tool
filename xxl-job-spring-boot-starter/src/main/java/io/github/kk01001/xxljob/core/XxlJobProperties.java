package io.github.kk01001.xxljob.core;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author kk01001
 * @date 2022/2/8 14:30
 * xxljob 属性
 */
@Data
@ConfigurationProperties(prefix = "xxl-job")
public class XxlJobProperties {

    private Boolean enable;

    private String accessToken;

    /**
     * 调度中心, 多个以,隔开
     */
    private String adminAddresses;

    private Integer logRetentionDays;

    private String logPath;

    private String address;

    private String ip;

    private Integer port;

    private String appName;

    private String title;

    private String userName;

    private String password;
}
