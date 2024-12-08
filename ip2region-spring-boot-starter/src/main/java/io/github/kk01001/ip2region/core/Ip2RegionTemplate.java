package io.github.kk01001.ip2region.core;

import lombok.extern.slf4j.Slf4j;
import org.lionsoul.ip2region.xdb.Searcher;

/**
 * @author linshiqiang
 * @date 2024-12-08 17:38:00
 * @description
 */
@Slf4j
public class Ip2RegionTemplate {

    private final Searcher searcher;

    public Ip2RegionTemplate(Searcher searcher) {
        this.searcher = searcher;
    }

    public RegionResult search(String ip) {
        try {
            String rawString = searcher.search(ip);
            return RegionResult.fromRawString(rawString);
        } catch (Exception e) {
            log.error("ip: {}, ip2region search error", ip, e);
        }
        return null;
    }
}
