package io.github.kk01001.script.exception;

/**
 * @author kk01001
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