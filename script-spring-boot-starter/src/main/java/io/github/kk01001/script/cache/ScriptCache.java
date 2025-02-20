package io.github.kk01001.script.cache;

import io.github.kk01001.script.enums.ScriptType;
import lombok.Data;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author kk01001
 * @date 2025-02-19 15:30:00
 * @description 脚本缓存管理器
 */
@Slf4j
public class ScriptCache {
    private final Map<String, CachedScript> scriptCache = new ConcurrentHashMap<>();
    
    @Data
    @Builder
    public static class CachedScript {
        private String scriptContent;
        private Object compiledScript;
        private ScriptType type;
        private LocalDateTime lastUpdateTime;
    }
    
    /**
     * 获取缓存的脚本
     */
    public Optional<CachedScript> get(String scriptId) {
        return Optional.ofNullable(scriptCache.get(scriptId));
    }
    
    /**
     * 缓存脚本
     */
    public void put(String scriptId, String content, Object compiledScript, ScriptType type) {
        scriptCache.put(scriptId, CachedScript.builder()
                .scriptContent(content)
                .compiledScript(compiledScript)
                .type(type)
                .lastUpdateTime(LocalDateTime.now())
                .build());
    }
    
    /**
     * 移除脚本
     */
    public void remove(String scriptId) {
        scriptCache.remove(scriptId);
    }
    
    /**
     * 清空缓存
     */
    public void clear() {
        scriptCache.clear();
    }
} 