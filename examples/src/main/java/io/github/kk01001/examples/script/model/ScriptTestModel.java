package io.github.kk01001.examples.script.model;

import io.github.kk01001.script.enums.ScriptType;
import lombok.Data;

import java.util.Map;

/**
 * @author kk01001
 * @date 2025-02-19 15:30:00
 * @description 脚本测试模型
 */
@Data
public class ScriptTestModel {
    /**
     * 脚本ID
     */
    private String scriptId;
    
    /**
     * 脚本类型
     */
    private ScriptType type;
    
    /**
     * 脚本内容
     */
    private String script;
    
    /**
     * 脚本参数
     */
    private Map<String, Object> params;
} 