package io.github.kk01001.ip2region;

import io.github.kk01001.ip2region.core.Ip2RegionProperties;
import io.github.kk01001.ip2region.core.Ip2RegionTemplate;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.lionsoul.ip2region.xdb.Searcher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

/**
 * @author linshiqiang
 * @date 2024-12-08 17:08:00
 * @description <a href="https://github.com/lionsoul2014/ip2region/blob/master/data/ip2region.xdb">...</a>
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(Ip2RegionProperties.class)
@ConditionalOnProperty(prefix = "ip2region", name = "enabled", havingValue = "true")
public class Ip2RegionConfiguration {

    @SneakyThrows
    @Bean
    public Searcher searcher(Ip2RegionProperties ip2RegionProperties) {
        String dbPath = ip2RegionProperties.getDbPath();
        Assert.notNull(dbPath, "dbPath must not be null");
        byte[] cBuff = Searcher.loadContentFromFile(dbPath);
        return Searcher.newWithBuffer(cBuff);
    }

    @Bean
    public Ip2RegionTemplate ip2RegionTemplate(Searcher searcher) {
        return new Ip2RegionTemplate(searcher);
    }

}
