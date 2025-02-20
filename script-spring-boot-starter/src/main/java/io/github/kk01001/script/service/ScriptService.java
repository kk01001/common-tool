package io.github.kk01001.script.service;

import io.github.kk01001.script.cache.ScriptCache;
import io.github.kk01001.script.enums.ScriptType;
import io.github.kk01001.script.executor.ScriptExecutor;
import io.github.kk01001.script.exception.ScriptValidateException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;

/**
 * @author kk01001
 * @date 2025-02-19 15:30:00
 * @description 脚本执行服务
 */
@Slf4j
@RequiredArgsConstructor
public class ScriptService {
    private final Map<ScriptType, ScriptExecutor> executors;
    private final ScriptCache scriptCache;
    
    /**
     * 执行脚本
     *
     * @param scriptId 脚本ID
     * @param type 脚本类型
     * @param script 脚本内容
     * @param params 参数
     * @return 执行结果
     */
    public Object execute(String scriptId, ScriptType type, String script, Map<String, Object> params) {
        ScriptExecutor executor = executors.get(type);
        if (executor == null) {
            throw new IllegalArgumentException("Unsupported script type: " + type);
        }

        Optional<ScriptCache.CachedScript> cachedScript = scriptCache.get(scriptId);
        if (cachedScript.isPresent() && script.equals(cachedScript.get().getScriptContent())) {
            return executor.executeCompiled(cachedScript.get().getCompiledScript(), params);
        }

        Object compiledScript = executor.compile(script);
        scriptCache.put(scriptId, script, compiledScript, type);
        return executor.executeCompiled(compiledScript, params);
    }
    
    /**
     * 刷新脚本
     */
    public void refresh(String scriptId, ScriptType type, String script) {
        ScriptExecutor executor = executors.get(type);
        if (executor == null) {
            throw new IllegalArgumentException("Unsupported script type: " + type);
        }
        
        Object compiledScript = executor.compile(script);
        scriptCache.put(scriptId, script, compiledScript, type);
    }
    
    /**
     * 移除脚本
     */
    public void remove(String scriptId) {
        scriptCache.remove(scriptId);
    }

    /**
     * 校验脚本
     *
     * @param type 脚本类型
     * @param script 脚本内容
     * @throws ScriptValidateException 校验失败时抛出异常
     */
    public void validate(ScriptType type, String script) throws ScriptValidateException {
        ScriptExecutor executor = executors.get(type);
        if (executor == null) {
            throw new IllegalArgumentException("Unsupported script type: " + type);
        }
        executor.validate(script);
    }
} 