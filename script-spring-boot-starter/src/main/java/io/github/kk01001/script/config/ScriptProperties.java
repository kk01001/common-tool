package io.github.kk01001.script.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "script")
public class ScriptProperties {
    /**
     * 是否启用脚本执行
     */
    private boolean enabled = true;
    
    /**
     * 缓存大小
     */
    private int cacheSize = 1000;
    
    /**
     * 脚本执行超时时间(毫秒)
     */
    private long timeout = 5000;
    
    /**
     * Groovy配置
     */
    private GroovyProperties groovy = new GroovyProperties();
    
    /**
     * JavaScript配置
     */
    private JavaScriptProperties javaScript = new JavaScriptProperties();
    
    /**
     * Lua配置
     */
    private LuaProperties lua = new LuaProperties();
    
    @Data
    public static class GroovyProperties {
        /**
         * 是否启用
         */
        private boolean enabled = true;
        
        /**
         * 编译缓存大小
         */
        private int cacheSize = 100;
    }
    
    @Data
    public static class JavaScriptProperties {
        /**
         * 是否启用
         */
        private boolean enabled = true;
        
        /**
         * 是否启用严格模式
         */
        private boolean strictMode = true;
    }
    
    @Data
    public static class LuaProperties {
        /**
         * 是否启用
         */
        private boolean enabled = true;
        
        /**
         * 是否启用沙箱模式
         */
        private boolean sandbox = true;
    }
} 