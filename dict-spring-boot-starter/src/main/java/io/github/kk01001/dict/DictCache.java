package io.github.kk01001.dict;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class DictCache {
    
    private static DictLoader dictLoader;
    private static JdbcTemplate jdbcTemplate;
    
    private static final Map<String, Map<String, String>> CACHE = new ConcurrentHashMap<>();

    public static void init(DictLoader loader, JdbcTemplate template) {
        dictLoader = loader;
        jdbcTemplate = template;
        // 初始化时加载所有字典
        refreshAll();
    }

    public static String getDictText(String type, String value, String table, String field) {
        if (value == null) {
            return null;
        }
        
        // 先从缓存获取
        Map<String, String> dictMap = CACHE.get(type);
        if (dictMap != null) {
            // 如果缓存中存在，直接返回
            return dictMap.getOrDefault(value, null);
        }

        // 如果缓存中没有，则从加载器加载
        dictMap = dictLoader.loadDict(type);
        if (dictMap != null) {
            CACHE.put(type, dictMap);
            // 从加载器获取到的字典返回
            return dictMap.getOrDefault(value, null);
        }

        // 如果都没有，判断是否指定了 table 和 field
        if (!table.isEmpty() && !field.isEmpty()) {
            // 从数据库中查询对应的文本
            return queryFromDatabase(table, field, value);
        }

        // 如果都没有找到，返回 null
        return null;
    }

    private static String queryFromDatabase(String table, String field, String value) {
        String sql = String.format("SELECT %s FROM %s WHERE %s = ?", field, table, field);
        return jdbcTemplate.queryForObject(sql, String.class);
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