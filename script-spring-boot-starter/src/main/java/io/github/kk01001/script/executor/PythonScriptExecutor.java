package io.github.kk01001.script.executor;

import io.github.kk01001.script.enums.ScriptType;
import io.github.kk01001.script.exception.ScriptCompileException;
import io.github.kk01001.script.exception.ScriptExecuteException;
import io.github.kk01001.script.exception.ScriptValidateException;
import lombok.extern.slf4j.Slf4j;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

import java.util.Map;

/**
 * @author kk01001
 * @date 2025-02-19 15:30:00
 * @description Python脚本执行器
 */
@Slf4j
public class PythonScriptExecutor implements ScriptExecutor {
    private final PythonInterpreter interpreter;

    public PythonScriptExecutor() {
        this.interpreter = new PythonInterpreter();
    }

    @Override
    public ScriptType getType() {
        return ScriptType.PYTHON;
    }

    @Override
    public Object execute(String script, Map<String, Object> params) {
        PyObject compiledScript = compile(script);
        return executeCompiled(compiledScript, params);
    }

    @Override
    public Object executeMethod(String script, String methodName, Map<String, Object> params) {
        return null;
    }

    @Override
    public PyObject compile(String script) {
        try {
            return interpreter.compile(script);
        } catch (Exception e) {
            log.error("Python script compilation failed", e);
            throw new ScriptCompileException("Python script compilation failed", e);
        }
    }

    @Override
    public Object executeCompiled(Object compiledScript, Map<String, Object> params) {
        try {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                interpreter.set(entry.getKey(), entry.getValue());
            }
            return ((PyObject) compiledScript).__call__();
        } catch (Exception e) {
            log.error("Python script execution failed", e);
            throw new ScriptExecuteException("Python script execution failed", e);
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
            interpreter.compile(script);
        } catch (Exception e) {
            throw new ScriptValidateException("Python script validation failed: " + e.getMessage(), e);
        }
    }
} 