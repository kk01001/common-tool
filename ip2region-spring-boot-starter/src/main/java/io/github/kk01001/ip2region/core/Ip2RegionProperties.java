package io.github.kk01001.ip2region.core;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author linshiqiang
 * @date 2024-12-08 17:20:00
 * @description
 */
@Data
@ConfigurationProperties(prefix = "ip2region")
public class Ip2RegionProperties {

    /**
     * 是否开启
     */
    private Boolean enabled = true;

    /**
     * 数据库文件
     * <a href="https://github.com/lionsoul2014/ip2region/blob/master/data/ip2region.xdb">...</a>
     */
    private String dbPath;

}
