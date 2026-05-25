package io.github.archer099.script.exception;

/**
 * @author archer099
 * @date 2025-02-19 15:30:00
 * @description 脚本执行异常
 */
public class ScriptExecuteException extends RuntimeException {
    
    public ScriptExecuteException(String message) {
        super(message);
    }
    
    public ScriptExecuteException(String message, Throwable cause) {
        super(message, cause);
    }
} 