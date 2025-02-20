package io.github.kk01001.script.executor;

import groovy.lang.Binding;
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
        Script compiledScript = compile(script);
        return executeCompiled(compiledScript, params);
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
        try {
            Script script = (Script) compiledScript;
            Binding binding = new Binding(params);
            script.setBinding(binding);
            return script.run();
        } catch (Exception e) {
            log.error("Groovy script execution failed", e);
            throw new ScriptExecuteException("Groovy script execution failed", e);
        }
    }
} 