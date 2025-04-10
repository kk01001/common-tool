# Script Spring Boot Starter

[![Maven Central](https://img.shields.io/maven-central/v/io.github.kk01001/script-spring-boot-starter.svg)](https://search.maven.org/artifact/io.github.kk01001/script-spring-boot-starter)
[![License](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.txt)

## 简介

`script-spring-boot-starter` 是一个用于在 Spring Boot 应用中轻松集成多种脚本语言执行能力的工具包。该组件支持 Groovy、JavaScript、Lua、Python 和 Java 脚本的动态编译和执行，提供统一的 API 接口、缓存管理和参数传递机制，让您能够轻松地在应用中嵌入脚本执行功能。

## 功能特点

- 🔌 **多语言支持**：支持 Groovy、JavaScript、Lua、Python 和 Java 多种脚本语言
- 🧩 **统一 API**：提供统一的脚本执行接口，简化多语言脚本调用
- 📦 **自动装配**：基于 Spring Boot 自动配置，零代码即可集成
- 💾 **缓存机制**：内置脚本编译缓存，提高执行效率
- 🔒 **安全验证**：支持脚本内容校验，防止恶意代码执行
- 🧠 **参数传递**：灵活的参数传递机制，支持复杂对象和数据结构
- 🔍 **方法调用**：支持调用脚本中的指定方法

## 技术栈

- Java 21
- Spring Boot 3.x
- Groovy 4.x
- GraalVM JavaScript
- LuaJ
- Jython

## 快速开始

### 添加依赖

在你的 `pom.xml` 文件中添加以下依赖：

```xml
<dependency>
    <groupId>io.github.kk01001</groupId>
    <artifactId>script-spring-boot-starter</artifactId>
    <version>最新版本</version>
</dependency>
```

### 配置属性

在 `application.properties` 或 `application.yml` 中配置：

```yaml
script:
  # 是否启用脚本执行
  enabled: true
  # 脚本缓存大小
  cache-size: 1000
  # 脚本执行超时时间(毫秒)
  timeout: 5000
  
  # Groovy配置
  groovy:
    enabled: true
    cache-size: 100
  
  # JavaScript配置
  java-script:
    enabled: true
    strict-mode: true
  
  # Lua配置
  lua:
    enabled: true
    sandbox: true
```

### 注入服务

```java
import io.github.kk01001.script.service.ScriptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MyService {

    @Autowired
    private ScriptService scriptService;
    
    // 使用脚本服务...
}
```

## 使用示例

### 执行 Groovy 脚本

```java
import io.github.kk01001.script.enums.ScriptType;
import io.github.kk01001.script.service.ScriptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class GroovyScriptExample {

    @Autowired
    private ScriptService scriptService;
    
    public Object executeGroovyScript() {
        String scriptId = "sample-groovy-script";
        String script = """
            def execute(params) {
                def name = params.name ?: "Guest"
                return "Hello, ${name}! Today is ${new Date().format('yyyy-MM-dd')}"
            }
            """;
            
        Map<String, Object> params = new HashMap<>();
        params.put("name", "John");
        
        return scriptService.execute(scriptId, ScriptType.GROOVY, script, params);
    }
}
```

### 执行 JavaScript 脚本

```java
import io.github.kk01001.script.enums.ScriptType;
import io.github.kk01001.script.service.ScriptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class JavaScriptExample {

    @Autowired
    private ScriptService scriptService;
    
    public Object executeJavaScript() {
        String scriptId = "sample-js-script";
        String script = """
            const name = name || 'Guest';
            const message = `Hello, ${name}! Current timestamp: ${Date.now()}`;
            message;
            """;
            
        Map<String, Object> params = new HashMap<>();
        params.put("name", "Alice");
        
        return scriptService.execute(scriptId, ScriptType.JAVASCRIPT, script, params);
    }
}
```

### 执行 Lua 脚本

```java
import io.github.kk01001.script.enums.ScriptType;
import io.github.kk01001.script.service.ScriptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class LuaScriptExample {

    @Autowired
    private ScriptService scriptService;
    
    public Object executeLuaScript() {
        String scriptId = "sample-lua-script";
        String script = """
            local name = name or "Guest"
            return "Hello, " .. name .. "! From Lua script"
            """;
            
        Map<String, Object> params = new HashMap<>();
        params.put("name", "Bob");
        
        return scriptService.execute(scriptId, ScriptType.LUA, script, params);
    }
}
```

### 执行 Python 脚本

```java
import io.github.kk01001.script.enums.ScriptType;
import io.github.kk01001.script.service.ScriptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class PythonScriptExample {

    @Autowired
    private ScriptService scriptService;
    
    public Object executePythonScript() {
        String scriptId = "sample-python-script";
        String script = """
            import time
            
            def execute(params):
                name = params.get('name', 'Guest')
                return f"Hello, {name}! Current timestamp: {time.time()}"
            """;
            
        Map<String, Object> params = new HashMap<>();
        params.put("name", "Charlie");
        
        return scriptService.execute(scriptId, ScriptType.PYTHON, script, params);
    }
}
```

### 执行编译后的 Java 代码

```java
import io.github.kk01001.script.enums.ScriptType;
import io.github.kk01001.script.service.ScriptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class JavaScriptExample {

    @Autowired
    private ScriptService scriptService;
    
    public Object executeJavaCode() {
        String scriptId = "sample-java-code";
        String script = """
            import java.time.LocalDateTime;
            import java.util.Map;
            
            public class DynamicScript {
                public String execute(Map<String, Object> params) {
                    String name = (String) params.getOrDefault("name", "Guest");
                    return "Hello, " + name + "! Current time: " + LocalDateTime.now();
                }
            }
            """;
            
        Map<String, Object> params = new HashMap<>();
        params.put("name", "Dave");
        
        return scriptService.execute(scriptId, ScriptType.JAVA, script, params);
    }
}
```

## 高级特性

### 脚本验证

```java
import io.github.kk01001.script.enums.ScriptType;
import io.github.kk01001.script.exception.ScriptValidateException;
import io.github.kk01001.script.service.ScriptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ScriptValidationExample {

    @Autowired
    private ScriptService scriptService;
    
    public boolean validateScript(String script, ScriptType type) {
        try {
            scriptService.validate(type, script);
            return true;
        } catch (ScriptValidateException e) {
            // 处理验证失败
            return false;
        }
    }
}
```

### 脚本缓存管理

```java
import io.github.kk01001.script.cache.ScriptCache;
import io.github.kk01001.script.enums.ScriptType;
import io.github.kk01001.script.service.ScriptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ScriptCacheExample {

    @Autowired
    private ScriptService scriptService;
    
    public void manageScriptCache() {
        // 刷新脚本缓存
        String scriptId = "my-script";
        String script = "def execute(params) { return 'Hello World' }";
        scriptService.refresh(scriptId, ScriptType.GROOVY, script);
        
        // 移除特定脚本缓存
        scriptService.remove("old-script");
        
        // 清空所有脚本缓存
        ScriptCache.clear();
    }
}
```

### 调用脚本中的特定方法

```java
import io.github.kk01001.script.enums.ScriptType;
import io.github.kk01001.script.service.ScriptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class MethodCallExample {

    @Autowired
    private ScriptService scriptService;
    
    public Object callSpecificMethod() {
        String scriptId = "multi-method-script";
        String script = """
            def greet(params) {
                return "Hello, ${params.name}!"
            }
            
            def farewell(params) {
                return "Goodbye, ${params.name}!"
            }
            """;
            
        Map<String, Object> params = new HashMap<>();
        params.put("name", "Alice");
        
        // 调用特定方法
        return scriptService.executeMethod(scriptId, ScriptType.GROOVY, script, "farewell", params);
    }
}
```

## 最佳实践

### 脚本缓存设计

为避免重复编译脚本，组件使用了内置的缓存机制：

1. **基于ID缓存**：每个脚本都应该有一个唯一的ID，用于缓存查找
   ```java
   // 好的做法
   String scriptId = "user-welcome-script";
   scriptService.execute(scriptId, ScriptType.GROOVY, script, params);
   ```

2. **MD5校验**：系统使用脚本内容的MD5哈希值来判断脚本是否变更
   ```java
   // 内部实现自动处理脚本变更
   if (cachedScript.isPresent() && SecureUtil.md5(script).equals(cachedScript.get().getMd5())) {
       // 使用缓存的编译结果
   } else {
       // 重新编译脚本
   }
   ```

### 异常处理

合理处理脚本执行过程中可能出现的异常：

```java
try {
    Object result = scriptService.execute(scriptId, ScriptType.GROOVY, script, params);
    // 处理结果
} catch (ScriptCompileException e) {
    // 脚本编译失败
    logger.error("脚本编译失败", e);
} catch (ScriptExecuteException e) {
    // 脚本执行失败
    logger.error("脚本执行失败", e);
} catch (Exception e) {
    // 其他异常
    logger.error("脚本处理过程中发生未知错误", e);
}
```

### 参数传递

设计合理的参数传递方式：

```java
// 通过Map传递复杂参数
Map<String, Object> params = new HashMap<>();
params.put("user", userObject);
params.put("items", itemList);
params.put("config", configMap);

// 在Groovy脚本中访问
def execute(params) {
    def user = params.user
    def items = params.items
    // 使用参数...
}
```

## 常见问题

### Q: 如何处理脚本执行超时？
A: 当前版本依赖于JVM的中断机制，您可以使用Spring的`@Async`与`Future`结合来实现超时控制：

```java
@Async
public CompletableFuture<Object> executeWithTimeout(String scriptId, ScriptType type, String script, Map<String, Object> params) {
    return CompletableFuture.completedFuture(
        scriptService.execute(scriptId, type, script, params)
    );
}

// 使用
Future<Object> future = executeWithTimeout(scriptId, type, script, params);
try {
    Object result = future.get(timeout, TimeUnit.MILLISECONDS);
    // 处理结果
} catch (TimeoutException e) {
    // 处理超时
    future.cancel(true); // 尝试中断执行
}
```

### Q: 脚本执行的安全性如何保障？
A: 不同的脚本引擎有不同的安全机制：
- Groovy：可以通过配置`CompilerConfiguration`和自定义`SecureASTCustomizer`限制可用API
- JavaScript：GraalVM提供了沙箱控制机制
- Lua：LuaJ支持沙箱模式限制可访问的库
- Python：可以通过Jython的安全管理器限制权限

建议在生产环境中，谨慎执行来源不可信的脚本。

### Q: 如何扩展支持其他脚本语言？
A: 实现`ScriptExecutor`接口并注册到Spring容器中：

```java
@Component
public class MyScriptExecutor implements ScriptExecutor {
    @Override
    public ScriptType getType() {
        return ScriptType.valueOf("MY_SCRIPT"); // 需要扩展ScriptType枚举
    }
    
    // 实现其他接口方法...
}
```

## 许可证

本项目遵循 [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0) 开源许可证。

## 贡献指南

欢迎提交问题和贡献代码，请通过 GitHub Issue 和 Pull Request 参与项目开发。 