package io.github.kk01001.redisson.example.controller;

import io.github.kk01001.redisson.template.MultiRedissonTemplate;
import org.redisson.api.GeoEntry;
import org.redisson.api.GeoPosition;
import org.redisson.api.GeoUnit;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author kk01001
 * @date 2026-01-17 19:30:00
 * @description 地理位置 Demo - 附近的人/商家实战案例
 * <p>
 * 应用场景：
 * 1. 附近的人/商家/门店
 * 2. 外卖配送范围
 * 3. 打车距离计算
 * 4. 签到打卡范围判断
 * </p>
 */
@RestController
@RequestMapping("/api/geo")
public class GeoController {

    private static final String SHOP_KEY = "demo:geo:shops";
    private static final String USER_KEY = "demo:geo:users";

    private final MultiRedissonTemplate redissonTemplate;

    public GeoController(MultiRedissonTemplate redissonTemplate) {
        this.redissonTemplate = redissonTemplate;
    }

    /**
     * 初始化商家数据（北京部分商家）
     */
    @PostMapping("/shops/init")
    public Map<String, Object> initShops() {
        Map<String, Object> result = new LinkedHashMap<>();

        // 先删除旧数据
        redissonTemplate.delete(SHOP_KEY);

        // 添加一些北京的商家位置（经度, 纬度）
        long added = 0;
        added += redissonTemplate.addGeoLocation(SHOP_KEY, 116.397428, 39.90923, "天安门广场");
        added += redissonTemplate.addGeoLocation(SHOP_KEY, 116.403963, 39.915119, "王府井商城");
        added += redissonTemplate.addGeoLocation(SHOP_KEY, 116.410159, 39.908728, "东方广场");
        added += redissonTemplate.addGeoLocation(SHOP_KEY, 116.386665, 39.915856, "西单商场");
        added += redissonTemplate.addGeoLocation(SHOP_KEY, 116.473168, 39.992789, "望京SOHO");
        added += redissonTemplate.addGeoLocation(SHOP_KEY, 116.461628, 39.909382, "国贸商城");
        added += redissonTemplate.addGeoLocation(SHOP_KEY, 116.481053, 39.996755, "望京凯德Mall");
        added += redissonTemplate.addGeoLocation(SHOP_KEY, 116.310906, 39.994814, "中关村广场");
        added += redissonTemplate.addGeoLocation(SHOP_KEY, 116.331382, 39.97566, "海淀黄庄");
        added += redissonTemplate.addGeoLocation(SHOP_KEY, 116.434094, 39.908723, "三里屯太古里");

        result.put("success", true);
        result.put("addedCount", added);
        result.put("message", "商家数据初始化成功");

        return result;
    }

    /**
     * 添加位置
     */
    @PostMapping("/add")
    public Map<String, Object> addLocation(
            @RequestParam(defaultValue = "shops") String type,
            @RequestParam String name,
            @RequestParam double longitude,
            @RequestParam double latitude) {

        Map<String, Object> result = new LinkedHashMap<>();

        String key = "shops".equals(type) ? SHOP_KEY : USER_KEY;
        long added = redissonTemplate.addGeoLocation(key, longitude, latitude, name);

        result.put("success", added > 0);
        result.put("name", name);
        result.put("longitude", longitude);
        result.put("latitude", latitude);
        result.put("message", added > 0 ? "位置添加成功" : "位置已存在，已更新");

        return result;
    }

    /**
     * 获取位置坐标
     */
    @GetMapping("/position")
    public Map<String, Object> getPosition(
            @RequestParam(defaultValue = "shops") String type,
            @RequestParam String name) {

        Map<String, Object> result = new LinkedHashMap<>();

        String key = "shops".equals(type) ? SHOP_KEY : USER_KEY;
        Map<String, GeoPosition> positions = redissonTemplate.getGeoPosition(key, name);
        GeoPosition pos = positions.get(name);

        if (pos != null) {
            result.put("success", true);
            result.put("name", name);
            result.put("longitude", pos.getLongitude());
            result.put("latitude", pos.getLatitude());
        } else {
            result.put("success", false);
            result.put("message", "位置不存在");
        }

        return result;
    }

    /**
     * 计算两点距离
     */
    @GetMapping("/distance")
    public Map<String, Object> getDistance(
            @RequestParam(defaultValue = "shops") String type,
            @RequestParam String name1,
            @RequestParam String name2,
            @RequestParam(defaultValue = "METERS") String unit) {

        Map<String, Object> result = new LinkedHashMap<>();

        String key = "shops".equals(type) ? SHOP_KEY : USER_KEY;
        GeoUnit geoUnit = GeoUnit.valueOf(unit.toUpperCase());
        Double distance = redissonTemplate.getGeoDistance(key, name1, name2, geoUnit);

        if (distance != null) {
            result.put("success", true);
            result.put("from", name1);
            result.put("to", name2);
            result.put("distance", distance);
            result.put("unit", unit);
            result.put("message", String.format("%s 到 %s 的距离为 %.2f %s",
                    name1, name2, distance, unit.toLowerCase()));
        } else {
            result.put("success", false);
            result.put("message", "无法计算距离，请检查位置是否存在");
        }

        return result;
    }

    /**
     * 搜索附近的位置（按坐标）
     */
    @GetMapping("/nearby")
    public Map<String, Object> searchNearby(
            @RequestParam(defaultValue = "shops") String type,
            @RequestParam double longitude,
            @RequestParam double latitude,
            @RequestParam(defaultValue = "5") double radius,
            @RequestParam(defaultValue = "KILOMETERS") String unit,
            @RequestParam(defaultValue = "10") int count) {

        Map<String, Object> result = new LinkedHashMap<>();

        String key = "shops".equals(type) ? SHOP_KEY : USER_KEY;
        GeoUnit geoUnit = GeoUnit.valueOf(unit.toUpperCase());

        // 搜索附近，带距离
        Map<String, Double> nearbyWithDistance = redissonTemplate.searchGeoWithDistance(
                key, longitude, latitude, radius, geoUnit, count);

        List<Map<String, Object>> nearby = new ArrayList<>();
        nearbyWithDistance.forEach((name, distance) -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", name);
            item.put("distance", distance);
            item.put("unit", unit.toLowerCase());

            // 获取坐标
            Map<String, GeoPosition> positions = redissonTemplate.getGeoPosition(key, name);
            GeoPosition pos = positions.get(name);
            if (pos != null) {
                item.put("longitude", pos.getLongitude());
                item.put("latitude", pos.getLatitude());
            }
            nearby.add(item);
        });

        result.put("success", true);
        result.put("centerLongitude", longitude);
        result.put("centerLatitude", latitude);
        result.put("radius", radius);
        result.put("unit", unit);
        result.put("count", nearby.size());
        result.put("results", nearby);

        return result;
    }

    /**
     * 获取所有位置
     */
    @GetMapping("/list")
    public Map<String, Object> listAll(@RequestParam(defaultValue = "shops") String type) {
        Map<String, Object> result = new LinkedHashMap<>();

        String key = "shops".equals(type) ? SHOP_KEY : USER_KEY;

        // 搜索所有（使用大范围搜索）
        List<String> members = redissonTemplate.searchGeo(key, 116.4, 39.9, 500, GeoUnit.KILOMETERS);

        List<Map<String, Object>> locations = new ArrayList<>();
        for (String member : members) {
            Map<String, GeoPosition> positions = redissonTemplate.getGeoPosition(key, member);
            GeoPosition pos = positions.get(member);
            if (pos != null) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("name", member);
                item.put("longitude", pos.getLongitude());
                item.put("latitude", pos.getLatitude());
                locations.add(item);
            }
        }

        result.put("type", type);
        result.put("count", locations.size());
        result.put("locations", locations);

        return result;
    }

    /**
     * 删除位置
     */
    @PostMapping("/remove")
    public Map<String, Object> removeLocation(
            @RequestParam(defaultValue = "shops") String type,
            @RequestParam String name) {

        Map<String, Object> result = new LinkedHashMap<>();

        String key = "shops".equals(type) ? SHOP_KEY : USER_KEY;
        boolean removed = redissonTemplate.removeGeoLocation(key, name);

        result.put("success", removed);
        result.put("name", name);
        result.put("message", removed ? "位置已删除" : "位置不存在");

        return result;
    }

    /**
     * 更新用户位置（模拟打卡签到）
     */
    @PostMapping("/checkin")
    public Map<String, Object> checkin(
            @RequestParam String userId,
            @RequestParam double longitude,
            @RequestParam double latitude,
            @RequestParam(defaultValue = "116.397428") double targetLon,
            @RequestParam(defaultValue = "39.90923") double targetLat,
            @RequestParam(defaultValue = "500") double allowedDistance) {

        Map<String, Object> result = new LinkedHashMap<>();

        // 计算与目标位置的距离
        double distance = calculateDistance(latitude, longitude, targetLat, targetLon);

        result.put("userId", userId);
        result.put("userLongitude", longitude);
        result.put("userLatitude", latitude);
        result.put("targetLongitude", targetLon);
        result.put("targetLatitude", targetLat);
        result.put("distance", Math.round(distance));
        result.put("allowedDistance", allowedDistance);

        if (distance <= allowedDistance) {
            // 更新用户位置
            redissonTemplate.addGeoLocation(USER_KEY, longitude, latitude, userId);

            result.put("success", true);
            result.put("message", String.format("签到成功！距离目标 %.0f 米", distance));
        } else {
            result.put("success", false);
            result.put("message", String.format("签到失败！距离目标 %.0f 米，超出允许范围 %.0f 米",
                    distance, allowedDistance));
        }

        return result;
    }

    /**
     * 计算两点间距离（Haversine公式）
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // 地球半径（米）

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }
}
