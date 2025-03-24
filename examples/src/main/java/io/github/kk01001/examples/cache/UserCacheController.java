package io.github.kk01001.examples.cache;

import io.github.kk01001.cache.factory.LocalCaffeineCacheFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author kk01001
 * @date 2024-03-24 14:31:00
 * @description 缓存操作控制器
 */
@RestController
@RequestMapping("/api/cache")
@RequiredArgsConstructor
public class UserCacheController {

    /**
     * 获取缓存值
     */
    @GetMapping("/get/{key}")
    public Object get(@PathVariable String key) {
        return LocalCaffeineCacheFactory.getCache(UserCaffeineCache.class).get(key);
    }

    /**
     * 设置缓存值
     */
    @PostMapping("/set")
    public void set(@RequestParam String key, @RequestParam String value) {
        LocalCaffeineCacheFactory.getCache(UserCaffeineCache.class).put(key, value);
    }

    /**
     * 删除缓存值
     */
    @DeleteMapping("/remove/{key}")
    public void remove(@PathVariable String key) {
        LocalCaffeineCacheFactory.getCache(UserCaffeineCache.class).remove(key);
    }

    /**
     * 清空缓存
     */
    @DeleteMapping("/clear")
    public void clear() {
        LocalCaffeineCacheFactory.getCache(UserCaffeineCache.class).clear();
    }

    /**
     * 获取缓存大小
     */
    @GetMapping("/size")
    public long size() {
        return LocalCaffeineCacheFactory.getCache(UserCaffeineCache.class).size();
    }

    /**
     * 获取所有缓存统计信息
     */
    @GetMapping("/stats")
    public Map<String, Object> stats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("size", LocalCaffeineCacheFactory.getCache(UserCaffeineCache.class).size());
        return stats;
    }

    /**
     * 批量设置缓存值
     */
    @PostMapping("/batch-set")
    public void batchSet(@RequestBody Map<String, String> keyValues) {
        keyValues.forEach(LocalCaffeineCacheFactory.getCache(UserCaffeineCache.class)::put);
    }

    /**
     * 批量获取缓存值
     */
    @PostMapping("/batch-get")
    public Map<String, String> batchGet(@RequestBody String[] keys) {
        Map<String, String> result = new HashMap<>();
        for (String key : keys) {
            String value = LocalCaffeineCacheFactory.getCache(UserCaffeineCache.class).get(key);
            if (value != null) {
                result.put(key, value);
            }
        }
        return result;
    }

    /**
     * 批量删除缓存值
     */
    @DeleteMapping("/batch-remove")
    public void batchRemove(@RequestBody String[] keys) {
        for (String key : keys) {
            LocalCaffeineCacheFactory.getCache(UserCaffeineCache.class).remove(key);
        }
    }
} 