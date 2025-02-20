package io.github.kk01001.script.exception;

/**
 * @author kk01001
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