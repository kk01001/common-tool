package io.github.kk01001.script.config;

import io.github.kk01001.script.executor.GroovyScriptExecutor;
import io.github.kk01001.script.executor.JavaExecutor;
import io.github.kk01001.script.executor.JavaScriptExecutor;
import io.github.kk01001.script.executor.LuaScriptExecutor;
import io.github.kk01001.script.executor.PythonScriptExecutor;
import io.github.kk01001.script.executor.ScriptExecutor;
import io.github.kk01001.script.service.ScriptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author kk01001
 * @date 2025-02-19 15:30:00
 * @description 脚本执行自动配置
 */
@Slf4j
@Configuration
@AutoConfiguration
@ConditionalOnClass({ScriptExecutor.class})
@EnableConfigurationProperties(ScriptProperties.class)
@ConditionalOnProperty(prefix = "script", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ScriptAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "groovy.lang.GroovyShell")
    public GroovyScriptExecutor groovyScriptExecutor() {
        log.info("Initializing Groovy script executor");
        return new GroovyScriptExecutor();
    }
    
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "org.graalvm.polyglot.Context")
    public JavaScriptExecutor javaScriptExecutor() {
        log.info("Initializing JavaScript script executor");
        return new JavaScriptExecutor();
    }
    
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "org.luaj.vm2.LuaValue")
    public LuaScriptExecutor luaScriptExecutor() {
        log.info("Initializing Lua script executor");
        return new LuaScriptExecutor();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "org.python.util.PythonInterpreter")
    public PythonScriptExecutor pythonScriptExecutor() {
        log.info("Initializing Python script executor");
        return new PythonScriptExecutor();
    }
    
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "javax.tools.JavaCompiler")
    public JavaExecutor javaExecutor() {
        log.info("Initializing Java script executor");
        return new JavaExecutor();
    }
    

    @Bean
    @ConditionalOnMissingBean
    public ScriptService scriptService(List<ScriptExecutor> executors) {
        return new ScriptService(executors);
    }
} 