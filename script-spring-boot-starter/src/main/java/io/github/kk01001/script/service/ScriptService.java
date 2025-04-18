package io.github.kk01001.script.service;

import cn.hutool.core.util.StrUtil;
import io.github.kk01001.script.cache.ScriptCache;
import io.github.kk01001.script.enums.ScriptType;
import io.github.kk01001.script.exception.ScriptValidateException;
import io.github.kk01001.script.executor.ScriptExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @author kk01001
 * @date 2025-02-19 15:30:00
 * @description 脚本执行服务
 */
@Slf4j
@RequiredArgsConstructor
public class ScriptService implements CommandLineRunner {

    private final static Map<ScriptType, ScriptExecutor> EXECUTOR_MAP = new HashMap<>();

    private final List<ScriptExecutor> executors;

    @Override
    public void run(String... args) {
        executors.forEach(executor -> {
            EXECUTOR_MAP.put(executor.getType(), executor);
        });
    }

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
        return executeMethod(scriptId, type, script, null, params);
    }

    /**
     * 执行脚本
     *
     * @param scriptId   脚本ID
     * @param type       脚本类型
     * @param script     脚本内容
     * @param methodName 方法名
     * @param params     参数
     * @return 执行结果
     */
    public Object executeMethod(String scriptId, ScriptType type, String script, String methodName, Map<String, Object> params) {
        ScriptExecutor executor = getExecutor(type);

        Optional<ScriptCache.CachedScript> cachedScript = ScriptCache.get(scriptId);
        if (cachedScript.isPresent()) {
            if (StrUtil.isNotBlank(methodName)) {
                return executor.executeCompiledMethod(cachedScript.get().getCompiledScript(), methodName, params);
            }
            return executor.executeCompiled(cachedScript.get().getCompiledScript(), params);
        }

        Object compiledScript = executor.compile(script);
        ScriptCache.put(scriptId, script, compiledScript, type);
        if (StrUtil.isNotBlank(methodName)) {
            return executor.executeCompiledMethod(compiledScript, methodName, params);
        }
        return executor.executeCompiled(compiledScript, params);
    }

    /**
     * 刷新脚本
     */
    public void refresh(String scriptId, ScriptType type, String script) {
        ScriptExecutor executor = getExecutor(type);

        Object compiledScript = executor.compile(script);
        ScriptCache.put(scriptId, script, compiledScript, type);
    }

    /**
     * 移除脚本
     */
    public void remove(String scriptId) {
        ScriptCache.remove(scriptId);
    }

    /**
     * 校验脚本
     *
     * @param type 脚本类型
     * @param script 脚本内容
     * @throws ScriptValidateException 校验失败时抛出异常
     */
    public void validate(ScriptType type, String script) throws ScriptValidateException {
        ScriptExecutor executor = getExecutor(type);
        executor.validate(script);
    }

    /**
     * 添加脚本执行器
     *
     * @param executor 脚本执行器
     */
    public void addExecutor(ScriptExecutor executor) {
        if (executor == null) {
            throw new IllegalArgumentException("Executor cannot be null");
        }
        EXECUTOR_MAP.put(executor.getType(), executor);
        log.info("Added script executor for type: {}", executor.getType());
    }

    /**
     * 获取脚本执行器
     *
     * @param type 脚本类型
     * @return 脚本执行器
     */
    public ScriptExecutor getExecutor(ScriptType type) {
        ScriptExecutor executor = EXECUTOR_MAP.get(type);
        if (executor == null) {
            throw new IllegalArgumentException("Unsupported script type: " + type);
        }
        return executor;
    }

    /**
     * 获取所有支持的脚本类型
     *
     * @return 支持的脚本类型集合
     */
    public Set<ScriptType> getSupportedTypes() {
        return EXECUTOR_MAP.keySet();
    }

}