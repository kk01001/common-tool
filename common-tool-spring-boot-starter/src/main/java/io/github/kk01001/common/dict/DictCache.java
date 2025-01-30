package io.github.kk01001.common.dict;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DictCache {

    private static DictLoader dictLoader;

    private static final Map<String, Map<String, String>> CACHE = new ConcurrentHashMap<>();

    /**
     * 初始化字典加载器
     */
    public static void init(DictLoader loader) {
        dictLoader = loader;
    }

    /**
     * 获取字典文本
     */
    public static String getDictText(String type, String code) {
        if (!StringUtils.hasText(type) || !StringUtils.hasText(code)) {
            return null;
        }
        return CACHE.computeIfAbsent(type, key -> {
            Map<String, String> dict = dictLoader.loadDict(key);
            refresh(type, dict);
            log.debug("Dictionary loaded for type: {}, size: {}", key, dict.size());
            return dict;
        }).get(code);
    }

    /**
     * 刷新指定类型的字典缓存
     */
    public static void refresh(String type, Map<String, String> dict) {
        CACHE.put(type, dict);
        log.debug("Dictionary cache refreshed for type: {}, size: {}", type, dict.size());
    }

    /**
     * 刷新所有字典缓存
     */
    public static void refreshAll() {
        CACHE.clear();
        Map<String, Map<String, String>> allDict = dictLoader.loadAllDict();
        CACHE.putAll(allDict);
        log.debug("All dictionary cache refreshed, types: {}", allDict.keySet());
    }
} 