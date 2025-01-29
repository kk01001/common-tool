package io.github.kk01001.robot.script;

import io.github.kk01001.robot.config.SmsScriptProperties;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Groovy脚本执行器
 * 负责执行Groovy脚本
 */
@Slf4j
@RequiredArgsConstructor
public class GroovyScriptExecutor {
    
    /**
     * HTTP请求客户端
     */
    private final RestTemplate restTemplate;
    
    /**
     * 短信脚本配置
     */
    private final SmsScriptProperties scriptProperties;
    
    /**
     * 执行脚本
     *
     * @param provider 短信提供商标识
     * @param params 脚本执行参数
     * @return 脚本执行结果
     */
    public ScriptExecuteResult executeScript(String provider, Map<String, Object> params) {
        String script = scriptProperties.getScript(provider);
        if (script == null) {
            throw new IllegalStateException("SMS script not found for provider: " + provider);
        }
        
        try {
            // 创建Binding对象，传入参数
            Binding binding = new Binding();
            binding.setVariable("params", params);
            binding.setVariable("restTemplate", restTemplate);
            binding.setVariable("ScriptExecuteResult", ScriptExecuteResult.class);
            
            // 创建GroovyShell并执行脚本
            GroovyShell shell = new GroovyShell(binding);
            Object result = shell.evaluate(script);
            
            // 如果脚本返回的不是ScriptExecuteResult，则包装为成功结果
            if (!(result instanceof ScriptExecuteResult)) {
                return ScriptExecuteResult.success(result);
            }
            return (ScriptExecuteResult) result;
        } catch (Exception e) {
            log.error("Failed to execute SMS script for provider: {}", provider, e);
            return ScriptExecuteResult.fail("Failed to execute SMS script: " + e.getMessage(), e);
        }
    }
} 