package io.github.kk01001.robot.script;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Groovy脚本执行器
 * 负责管理和执行Groovy脚本
 */
@Slf4j
public class GroovyScriptExecutor {
    
    /**
     * 脚本缓存
     * key: 短信提供商标识
     * value: Groovy脚本内容
     */
    private final Map<String, String> scriptCache = new ConcurrentHashMap<>();
    
    /**
     * HTTP请求客户端
     */
    private final RestTemplate restTemplate;
    
    public GroovyScriptExecutor(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    /**
     * 更新脚本
     *
     * @param provider 短信提供商标识
     * @param script Groovy脚本内容
     */
    public void updateScript(String provider, String script) {
        scriptCache.put(provider, script);
        log.info("Updated SMS script for provider: {}", provider);
    }
    
    /**
     * 执行脚本
     *
     * @param provider 短信提供商标识
     * @param params 脚本执行参数
     * @return 执行结果
     */
    public Object executeScript(String provider, Map<String, Object> params) {
        String script = scriptCache.get(provider);
        if (script == null) {
            throw new IllegalStateException("SMS script not found for provider: " + provider);
        }
        
        try {
            // 创建Binding对象，传入参数
            Binding binding = new Binding();
            binding.setVariable("params", params);
            binding.setVariable("restTemplate", restTemplate);
            
            // 创建GroovyShell并执行脚本
            GroovyShell shell = new GroovyShell(binding);
            return shell.evaluate(script);
        } catch (Exception e) {
            log.error("Failed to execute SMS script for provider: {}", provider, e);
            throw new RuntimeException("Failed to execute SMS script: " + e.getMessage(), e);
        }
    }
    
    /**
     * 删除脚本
     *
     * @param provider 短信提供商标识
     */
    public void removeScript(String provider) {
        scriptCache.remove(provider);
        log.info("Removed SMS script for provider: {}", provider);
    }
} 