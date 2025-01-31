package io.github.kk01001.common.dict;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class DictCache {
    
    private static DictLoader dictLoader;
    
    private static final Map<String, Map<String, String>> CACHE = new ConcurrentHashMap<>();
    
    public static void init(DictLoader loader) {
        dictLoader = loader;
        // 初始化时加载所有字典
        refreshAll();
    }
    
    public static String getDictText(String type, String value) {
        if (value == null) {
            return null;
        }
        
        // 先从缓存获取
        Map<String, String> dictMap = CACHE.get(type);
        if (dictMap == null) {
            // 如果缓存中没有，则从加载器加载
            dictMap = dictLoader.loadDict(type);
            if (dictMap != null) {
                CACHE.put(type, dictMap);
            } else {
                return value;
            }
        }
        
        // 返回翻译后的文本，如果没有找到则返回原值
        return dictMap.getOrDefault(value, value);
    }
    
    public static void refresh(String type, Map<String, String> dict) {
        CACHE.put(type, dict);
    }
    
    public static void refreshAll() {
        if (dictLoader != null) {
            Map<String, Map<String, String>> allDict = dictLoader.loadAllDict();
            if (allDict != null) {
                CACHE.clear();
                CACHE.putAll(allDict);
            }
        }
    }
} 