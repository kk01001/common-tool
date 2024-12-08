package io.github.kk01001.ip2region.core;

import lombok.Data;

/**
 * @author linshiqiang
 * @date 2024-12-08 17:29:00
 * @description
 */
@Data
public class RegionResult {

    private String country;

    private String area;

    private String province;

    private String city;

    private String isp;

    public static RegionResult fromRawString(String rawString) {
        String[] parts = rawString.split("\\|");
        RegionResult result = new RegionResult();
        result.setCountry(parts[0]);
        result.setArea(parts[1]);
        result.setProvince(parts[2]);
        result.setCity(parts[3]);
        result.setIsp(parts[4]);
        return result;
    }
}
