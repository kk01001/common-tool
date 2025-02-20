package io.github.kk01001.script.exception;

/**
 * @author kk01001
 * @date 2025-02-19 15:30:00
 * @description 脚本校验异常
 */
public class ScriptValidateException extends RuntimeException {
    
    public ScriptValidateException(String message) {
        super(message);
    }
    
    public ScriptValidateException(String message, Throwable cause) {
        super(message, cause);
    }
} 