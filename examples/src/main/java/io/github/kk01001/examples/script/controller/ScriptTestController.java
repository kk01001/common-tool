package io.github.kk01001.examples.script.controller;

import io.github.kk01001.examples.script.model.ScriptTestModel;
import io.github.kk01001.script.enums.ScriptType;
import io.github.kk01001.script.service.ScriptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author kk01001
 * @date 2025-02-19 15:30:00
 * @description 脚本执行测试控制器
 */
@Slf4j
@RestController
@RequestMapping("/script")
@RequiredArgsConstructor
public class ScriptTestController {

    private final ScriptService scriptService;

    /**
     * 执行脚本
     */
    @PostMapping("/execute")
    public Object execute(@RequestBody ScriptTestModel model) {
        return scriptService.execute(
                model.getScriptId(),
                model.getType(),
                model.getScript(),
                model.getParams()
        );
    }

    /**
     * 测试执行Groovy脚本
     */
    @PostMapping("/groovy")
    public Object executeGroovy(@RequestParam String scriptId, @RequestBody String script) {
        Map<String, Object> params = new HashMap<>();
        params.put("phoneNumber", "13023828639");
        params.put("content", "hello");

        return scriptService.execute(scriptId, ScriptType.GROOVY, script, params);
    }

    /**
     * 测试执行JavaScript脚本
     */
    @PostMapping("/javascript")
    public Object executeJavaScript(@RequestParam String scriptId, @RequestBody String script) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "World");
        params.put("timestamp", System.currentTimeMillis());

        return scriptService.execute(scriptId, ScriptType.JAVASCRIPT, script, params);
    }

    /**
     * 测试执行Java脚本
     */
    @PostMapping("/java")
    public Object executeJava(@RequestParam String scriptId, @RequestBody String script) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "World");
        params.put("timestamp", System.currentTimeMillis());

        return scriptService.execute(scriptId, ScriptType.JAVA, script, params);
    }

    /**
     * 测试执行Python脚本
     */
    @PostMapping("/python")
    public Object executePython(@RequestParam String scriptId, @RequestBody String script) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "World");
        params.put("timestamp", System.currentTimeMillis());

        return scriptService.execute(scriptId, ScriptType.PYTHON, script, params);
    }

    /**
     * 测试执行Lua脚本
     */
    @PostMapping("/lua")
    public Object executeLua(@RequestParam String scriptId, @RequestBody String script) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "World");
        params.put("timestamp", System.currentTimeMillis());

        return scriptService.execute(scriptId, ScriptType.LUA, script, params);
    }

    /**
     * 校验脚本
     */
    @PostMapping("/validate")
    public void validate(@RequestParam ScriptType type, @RequestBody String script) {
        scriptService.validate(type, script);
    }

    /**
     * 刷新脚本
     */
    @PostMapping("/refresh")
    public void refresh(@RequestParam String scriptId,
                        @RequestParam ScriptType type,
                        @RequestBody String script) {
        scriptService.refresh(scriptId, type, script);
    }

    /**
     * 删除脚本
     */
    @DeleteMapping("/{scriptId}")
    public void remove(@PathVariable String scriptId) {
        scriptService.remove(scriptId);
    }
}