package io.github.kk01001.script.executor;

import io.github.kk01001.script.enums.ScriptType;
import io.github.kk01001.script.exception.ScriptCompileException;
import io.github.kk01001.script.exception.ScriptExecuteException;
import lombok.extern.slf4j.Slf4j;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.util.Map;

/**
 * @author kk01001
 * @date 2025-02-19 15:30:00
 * @description Lua脚本执行器
 */
@Slf4j
public class LuaScriptExecutor implements ScriptExecutor {
    private final Globals globals;
    
    public LuaScriptExecutor() {
        globals = JsePlatform.standardGlobals();
    }

    @Override
    public ScriptType getType() {
        return ScriptType.LUA;
    }

    @Override
    public Object execute(String script, Map<String, Object> params) {
        LuaValue compiledScript = compile(script);
        return executeCompiled(compiledScript, params);
    }

    @Override
    public LuaValue compile(String script) {
        try {
            return globals.load(script);
        } catch (Exception e) {
            log.error("Lua script compilation failed", e);
            throw new ScriptCompileException("Lua script compilation failed", e);
        }
    }

    @Override
    public Object executeMethod(String script, String methodName, Map<String, Object> params) {
        return null;
    }

    @Override
    public Object executeCompiled(Object compiledScript, Map<String, Object> params) {
        try {
            // 设置全局变量
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                globals.set(entry.getKey(), LuaValue.valueOf(entry.getValue().toString()));
            }
            
            LuaValue luaScript = (LuaValue) compiledScript;
            return luaScript.call().tojstring();
        } catch (Exception e) {
            log.error("Lua script execution failed", e);
            throw new ScriptExecuteException("Lua script execution failed", e);
        }
    }

    @Override
    public Object executeCompiledMethod(Object compiledScript, String methodName, Map<String, Object> params) {
        return null;
    }
}