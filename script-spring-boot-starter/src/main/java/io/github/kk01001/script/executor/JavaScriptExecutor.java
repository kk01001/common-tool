package io.github.kk01001.script.executor;

import io.github.kk01001.script.enums.ScriptType;
import io.github.kk01001.script.exception.ScriptCompileException;
import io.github.kk01001.script.exception.ScriptExecuteException;
import io.github.kk01001.script.exception.ScriptValidateException;
import lombok.extern.slf4j.Slf4j;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

import java.util.Map;

/**
 * @author kk01001
 * @date 2025-02-19 15:30:00
 * @description JavaScript脚本执行器
 */
@Slf4j
public class JavaScriptExecutor implements ScriptExecutor {
    private final Context context;

    public JavaScriptExecutor() {
        this.context = Context.newBuilder("js")
                .allowAllAccess(true)
                .build();
    }

    @Override
    public ScriptType getType() {
        return ScriptType.JAVASCRIPT;
    }

    @Override
    public Object execute(String script, Map<String, Object> params) {
        try {
            Value bindings = context.getBindings("js");
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                bindings.putMember(entry.getKey(), entry.getValue());
            }
            return context.eval("js", script);
        } catch (Exception e) {
            log.error("JavaScript execution failed", e);
            throw new ScriptExecuteException("JavaScript execution failed", e);
        }
    }

    @Override
    public Object executeMethod(String script, String methodName, Map<String, Object> params) {
        return null;
    }

    @Override
    public Object compile(String script) {
        try {
            return context.parse("js", script);
        } catch (Exception e) {
            log.error("JavaScript compilation failed", e);
            throw new ScriptCompileException("JavaScript compilation failed", e);
        }
    }

    @Override
    public Object executeCompiled(Object compiledScript, Map<String, Object> params) {
        try {
            Value bindings = context.getBindings("js");
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                bindings.putMember(entry.getKey(), entry.getValue());
            }
            return ((Value) compiledScript).execute();
        } catch (Exception e) {
            log.error("JavaScript execution failed", e);
            throw new ScriptExecuteException("JavaScript execution failed", e);
        }
    }

    @Override
    public Object executeCompiledMethod(Object compiledScript, String methodName, Map<String, Object> params) {
        return null;
    }

    @Override
    public void validate(String script) throws ScriptValidateException {
        try {
            // 检查语法
            context.parse("js", script);
        } catch (Exception e) {
            throw new ScriptValidateException("JavaScript validation failed: " + e.getMessage(), e);
        }
    }
}