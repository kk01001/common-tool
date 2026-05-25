package io.github.archer099.script.exception;

/**
 * @author archer099
 * @date 2025-02-19 15:30:00
 * @description 脚本编译异常
 */
public class ScriptCompileException extends RuntimeException {
    
    public ScriptCompileException(String message) {
        super(message);
    }
    
    public ScriptCompileException(String message, Throwable cause) {
        super(message, cause);
    }
} 