package io.github.kk01001.script.executor;

import io.github.kk01001.script.enums.ScriptType;
import io.github.kk01001.script.exception.ScriptValidateException;

import java.util.Map;

/**
 * @author kk01001
 * @date 2025-02-19 15:30:00
 * @description 脚本执行器接口
 */
public interface ScriptExecutor {
    /**
     * 获取脚本类型
     */
    ScriptType getType();
    
    /**
     * 执行脚本
     *
     * @param script 脚本内容
     * @param params 参数
     * @return 执行结果
     */
    Object execute(String script, Map<String, Object> params);

    /**
     * 执行脚本
     *
     * @param script     脚本内容
     * @param methodName 方法名
     * @param params     参数
     * @return 执行结果
     */
    Object executeMethod(String script, String methodName, Map<String, Object> params);

    /**
     * 编译脚本
     *
     * @param script 脚本内容
     * @return 编译后的脚本
     */
    Object compile(String script);
    
    /**
     * 执行已编译的脚本
     *
     * @param compiledScript 编译后的脚本
     * @param methodName 方法名
     * @param params 参数
     * @return 执行结果
     */
    Object executeCompiledMethod(Object compiledScript, String methodName, Map<String, Object> params);

    /**
     * 执行已编译的脚本
     *
     * @param compiledScript 编译后的脚本
     * @param params         参数
     * @return 执行结果
     */
    Object executeCompiled(Object compiledScript, Map<String, Object> params);

    /**
     * 校验脚本内容是否合法
     *
     * @param script 脚本内容
     * @throws ScriptValidateException 校验失败时抛出异常
     */
    default void validate(String script) throws ScriptValidateException {
        try {
            compile(script);
        } catch (Exception e) {
            throw new ScriptValidateException("Script validation failed: " + e.getMessage(), e);
        }
    }
} 