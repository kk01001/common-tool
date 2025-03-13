package io.github.kk01001.script.executor;

import groovy.lang.GroovyShell;
import groovy.lang.Script;
import io.github.kk01001.script.enums.ScriptType;
import io.github.kk01001.script.exception.ScriptCompileException;
import io.github.kk01001.script.exception.ScriptExecuteException;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.util.Map;

/**
 * @author kk01001
 * @date 2025-02-19 15:30:00
 * @description Groovy脚本执行器
 */
@Slf4j
public class GroovyScriptExecutor implements ScriptExecutor {
    private final GroovyShell groovyShell;
    
    public GroovyScriptExecutor() {
        CompilerConfiguration config = new CompilerConfiguration();
        config.setSourceEncoding("UTF-8");
        groovyShell = new GroovyShell(config);
    }

    @Override
    public ScriptType getType() {
        return ScriptType.GROOVY;
    }

    @Override
    public Object execute(String script, Map<String, Object> params) {
        return executeMethod(script, "execute", params);
    }

    @Override
    public Object executeMethod(String script, String methodName, Map<String, Object> params) {
        Script compiledScript = compile(script);
        return executeCompiledMethod(compiledScript, methodName, params);
    }

    @Override
    public Script compile(String script) {
        try {
            return groovyShell.parse(script);
        } catch (Exception e) {
            log.error("Groovy script compilation failed", e);
            throw new ScriptCompileException("Groovy script compilation failed", e);
        }
    }

    @Override
    public Object executeCompiled(Object compiledScript, Map<String, Object> params) {
        return executeCompiledMethod(compiledScript, "execute", params);
    }

    @Override
    public Object executeCompiledMethod(Object compiledScript, String methodName, Map<String, Object> params) {
        try {
            Script script = (Script) compiledScript;
            return script.invokeMethod(methodName, new Object[]{params});
        } catch (Exception e) {
            log.error("Groovy script execution failed", e);
            throw new ScriptExecuteException("Groovy script execution failed", e);
        }
    }
}