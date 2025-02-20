package io.github.kk01001.examples.script.examples;

/**
 * @author kk01001
 * @date 2025-02-19 15:30:00
 * @description 脚本示例
 */
public class ScriptExamples {

    // Groovy脚本示例
    public static final String GROOVY_SCRIPT = """
            def name = params.name
            def timestamp = params.timestamp
            return "Hello ${name} at ${timestamp}"
            """;

    // JavaScript脚本示例
    public static final String JAVASCRIPT_SCRIPT = """
            const name = params.name;
            const timestamp = params.timestamp;
            `Hello ${name} at ${timestamp}`;
            """;

    // Java脚本示例
    public static final String JAVA_SCRIPT = """
            public class TestScript {
                public Object execute(Map<String, Object> params) {
                    String name = (String) params.get("name");
                    Long timestamp = (Long) params.get("timestamp");
                    return "Hello " + name + " at " + timestamp;
                }
            }
            """;

    // Python脚本示例
    public static final String PYTHON_SCRIPT = """
            name = params["name"]
            timestamp = params["timestamp"]
            "Hello %s at %s" % (name, timestamp)
            """;

    // Lua脚本示例
    public static final String LUA_SCRIPT = """
            local name = params.name
            local timestamp = params.timestamp
            return string.format("Hello %s at %s", name, timestamp)
            """;
}