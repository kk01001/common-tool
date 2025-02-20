package io.github.kk01001.script.executor;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ClassLoaderUtil;
import cn.hutool.core.util.StrUtil;
import io.github.kk01001.script.enums.ScriptType;
import io.github.kk01001.script.exception.ScriptCompileException;
import io.github.kk01001.script.exception.ScriptExecuteException;
import io.github.kk01001.script.exception.ScriptValidateException;
import lombok.extern.slf4j.Slf4j;

import javax.tools.*;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Map;

/**
 * @author kk01001
 * @date 2025-02-19 15:30:00
 * @description Java脚本执行器
 */
@Slf4j
public class JavaExecutor implements ScriptExecutor {
    private final JavaCompiler compiler;
    private final String tempDir;

    public JavaExecutor() {
        this.compiler = ToolProvider.getSystemJavaCompiler();
        this.tempDir = System.getProperty("java.io.tmpdir") + "/java_scripts";
        FileUtil.mkdir(this.tempDir);
    }

    @Override
    public ScriptType getType() {
        return ScriptType.JAVA;
    }

    @Override
    public Object execute(String script, Map<String, Object> params) {
        Object compiledScript = compile(script);
        return executeCompiled(compiledScript, params);
    }

    @Override
    public Object compile(String script) {
        try {
            // 解析类名
            String className = extractClassName(script);
            if (StrUtil.isBlank(className)) {
                throw new ScriptCompileException("Cannot find class name in script");
            }

            // 创建源文件
            File sourceFile = new File(tempDir, className + ".java");
            FileUtil.writeUtf8String(script, sourceFile);

            // 编译选项
            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
            Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(sourceFile));

            // 编译任务
            JavaCompiler.CompilationTask task = compiler.getTask(
                null,
                fileManager,
                diagnostics,
                Arrays.asList("-d", tempDir),
                null,
                compilationUnits
            );

            // 执行编译
            boolean success = task.call();
            if (!success) {
                StringBuilder errorMsg = new StringBuilder("Compilation failed:\n");
                for (Diagnostic<?> diagnostic : diagnostics.getDiagnostics()) {
                    errorMsg.append(diagnostic.getMessage(null)).append("\n");
                }
                throw new ScriptCompileException(errorMsg.toString());
            }

            // 返回编译信息
            return new CompiledJavaScript(className, tempDir);
        } catch (Exception e) {
            log.error("Java script compilation failed", e);
            throw new ScriptCompileException("Java script compilation failed", e);
        }
    }

    @Override
    public Object executeCompiled(Object compiledScript, Map<String, Object> params) {
        try {
            CompiledJavaScript script = (CompiledJavaScript) compiledScript;
            
            // 创建类加载器
            URL[] urls = new URL[]{new File(script.getClassPath()).toURI().toURL()};
            try (URLClassLoader classLoader = new URLClassLoader(urls, ClassLoader.getSystemClassLoader())) {
                // 加载类
                Class<?> clazz = classLoader.loadClass(script.getClassName());
                
                // 创建实例
                Object instance = clazz.getDeclaredConstructor().newInstance();
                
                // 查找并执行execute方法
                Method executeMethod = clazz.getMethod("execute", Map.class);
                return executeMethod.invoke(instance, params);
            }
        } catch (Exception e) {
            log.error("Java script execution failed", e);
            throw new ScriptExecuteException("Java script execution failed", e);
        }
    }

    private String extractClassName(String script) {
        // 简单的类名提取，可以根据需要改进
        String[] lines = script.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.contains("public class ")) {
                String[] parts = line.split("\\s+");
                for (int i = 0; i < parts.length - 1; i++) {
                    if (parts[i].equals("class")) {
                        return parts[i + 1];
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void validate(String script) throws ScriptValidateException {
        try {
            // 检查类名
            String className = extractClassName(script);
            if (StrUtil.isBlank(className)) {
                throw new ScriptValidateException("Cannot find class name in script");
            }

            // 检查是否包含execute方法
            if (!script.contains("public Object execute(Map<String, Object> params)")) {
                throw new ScriptValidateException("Script must contain 'public Object execute(Map<String, Object> params)' method");
            }

            // 尝试编译
            compile(script);
        } catch (Exception e) {
            if (e instanceof ScriptValidateException) {
                throw e;
            }
            throw new ScriptValidateException("Java script validation failed: " + e.getMessage(), e);
        }
    }

    /**
     * 编译后的Java脚本信息
     */
    private static class CompiledJavaScript {
        private final String className;
        private final String classPath;

        public CompiledJavaScript(String className, String classPath) {
            this.className = className;
            this.classPath = classPath;
        }

        public String getClassName() {
            return className;
        }

        public String getClassPath() {
            return classPath;
        }
    }
} 