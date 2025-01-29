package io.github.kk01001.robot.script;

import lombok.Builder;
import lombok.Data;

/**
 * 脚本执行结果
 */
@Data
@Builder
public class ScriptExecuteResult {
    
    /**
     * 是否执行成功
     */
    private boolean success;
    
    /**
     * 原始返回结果
     */
    private Object result;
    
    /**
     * 失败信息
     */
    private String failMessage;
    
    /**
     * 创建成功结果
     */
    public static ScriptExecuteResult success(Object result) {
        return ScriptExecuteResult.builder()
            .success(true)
            .result(result)
            .build();
    }
    
    /**
     * 创建失败结果
     * 
     * @param message 失败信息
     * @param result 原始返回结果，可以是异常信息或API返回的错误响应
     */
    public static ScriptExecuteResult fail(String message, Object result) {
        return ScriptExecuteResult.builder()
            .success(false)
            .failMessage(message)
            .result(result)
            .build();
    }
} 