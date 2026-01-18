# Sensitive Word Example

敏感词过滤组件 (sensitive-word-spring-boot-starter) 示例项目。

## 功能演示

- 文本敏感词检测
- 敏感词替换
- 敏感词高亮
- 注解驱动的敏感词检测（方法级/字段级）
- 动态敏感词管理（添加/移除）
- 白名单管理

## 快速启动

```bash
# 在项目根目录执行
mvn clean install -DskipTests

# 启动示例
cd examples-starter/sensitive-word-example
mvn spring-boot:run
```

访问 http://localhost:8080 查看演示界面。

## API 接口

### 文本检测

```bash
# 检测敏感词
curl -X POST http://localhost:8080/api/sensitive/check \
  -H "Content-Type: application/json" \
  -d '{"text": "这是一段包含敏感词的文本"}'

# 替换敏感词
curl -X POST http://localhost:8080/api/sensitive/replace \
  -H "Content-Type: application/json" \
  -d '{"text": "这是一段包含敏感词的文本", "replaceChar": "*"}'

# 高亮敏感词
curl -X POST http://localhost:8080/api/sensitive/highlight \
  -H "Content-Type: application/json" \
  -d '{"text": "这是一段包含敏感词的文本"}'
```

### 注解测试

```bash
# 检测（抛异常）
curl -X POST http://localhost:8080/api/sensitive/checkWithAnnotation \
  -H "Content-Type: application/json" \
  -d '"这是包含敏感词的文本"'

# 检测（自动替换）
curl -X POST http://localhost:8080/api/sensitive/replaceWithAnnotation \
  -H "Content-Type: application/json" \
  -d '"这是包含敏感词的文本"'
```

### 敏感词管理

```bash
# 添加敏感词
curl -X POST http://localhost:8080/api/sensitive/addWord \
  -H "Content-Type: application/json" \
  -d '{"word": "新敏感词", "category": "自定义"}'

# 移除敏感词
curl -X POST http://localhost:8080/api/sensitive/removeWord \
  -H "Content-Type: application/json" \
  -d '{"word": "新敏感词"}'

# 获取敏感词数量
curl http://localhost:8080/api/sensitive/count

# 添加白名单
curl -X POST http://localhost:8080/api/sensitive/addWhiteList \
  -H "Content-Type: application/json" \
  -d '{"word": "测试"}'
```

## 配置说明

```yaml
sensitive-word:
  enabled: true              # 是否启用
  ignore-case: true          # 是否忽略大小写
  skip-whitespace: true      # 是否跳过空白字符
  match-type: MIN_MATCH      # 匹配类型: MIN_MATCH/MAX_MATCH
  handle-type: REPLACE       # 处理类型: REPLACE/EXCEPTION/HIGHLIGHT/DETECT_ONLY
  replace-char: '*'          # 替换字符
  words:                     # 直接配置敏感词
    - 敏感词1
    - 敏感词2
  white-list:                # 白名单
    - 测试
```

## 注解使用

### 方法级注解

```java
// 检测到敏感词抛出异常
@SensitiveWordCheck(handleType = HandleType.EXCEPTION)
public void check(String content) { }

// 自动替换敏感词
@SensitiveWordCheck(handleType = HandleType.REPLACE, replaceChar = '*')
public String replace(String content) { }
```

### 字段级注解

```java
public class Article {
    @SensitiveWordField(handleType = HandleType.REPLACE)
    private String title;
    
    @SensitiveWordField(handleType = HandleType.REPLACE)
    private String content;
}
```
