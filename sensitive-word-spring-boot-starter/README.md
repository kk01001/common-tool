# Sensitive Word Spring Boot Starter

敏感词过滤组件，基于 DFA（确定性有限自动机）算法实现高性能敏感词检测和过滤。

## 特性

- **高性能**：基于 DFA 算法，时间复杂度 O(n)，适合大量文本检测
- **线程安全**：内置读写锁，支持多线程并发操作
- **多种匹配模式**：支持最小匹配和最大匹配两种模式
- **多种处理策略**：支持替换、抛异常、高亮、仅检测四种处理方式
- **注解驱动**：通过注解轻松实现方法参数和对象字段的敏感词检测
- **白名单支持**：支持配置白名单词汇，避免误杀
- **词库管理**：支持从配置、文件、classpath 多种方式加载词库
- **动态管理**：支持运行时动态添加、删除敏感词
- **分类支持**：支持为敏感词设置分类标签
- **忽略大小写**：可配置是否忽略大小写
- **跳过字符**：可配置跳过空白字符和特殊符号

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>io.github.archer099</groupId>
    <artifactId>sensitive-word-spring-boot-starter</artifactId>
    <version>${version}</version>
</dependency>
```

### 2. 配置敏感词

在 `application.yml` 中配置：

```yaml
sensitive-word:
  enabled: true
  # 是否忽略大小写
  ignore-case: true
  # 是否跳过空白字符
  skip-whitespace: true
  # 默认匹配类型：MIN_MATCH（最小匹配）、MAX_MATCH（最大匹配）
  match-type: MIN_MATCH
  # 默认处理类型：REPLACE（替换）、EXCEPTION（抛异常）、HIGHLIGHT（高亮）、DETECT_ONLY（仅检测）
  handle-type: REPLACE
  # 替换字符
  replace-char: '*'
  # 直接配置敏感词
  words:
    - 敏感词1
    - 敏感词2
    - 敏感词3
  # 白名单（不进行过滤的词汇）
  white-list:
    - 白名单词汇1
    - 白名单词汇2
  # 内置词库路径（classpath 下）
  dict-paths:
    - sensitive/default.txt
    - sensitive/politics.txt
  # 外部词库路径（文件系统）
  external-dict-paths:
    - /data/sensitive/custom.txt
```

### 3. 使用服务类

```java
@Service
@RequiredArgsConstructor
public class ContentService {
    
    private final SensitiveWordService sensitiveWordService;
    
    public void checkContent(String content) {
        // 检测是否包含敏感词
        boolean contains = sensitiveWordService.contains(content);
        
        // 查找所有敏感词
        List<SensitiveWordResult> results = sensitiveWordService.findAll(content);
        
        // 替换敏感词
        String filtered = sensitiveWordService.replace(content);
        
        // 高亮敏感词
        String highlighted = sensitiveWordService.highlight(content);
        
        // 根据配置的策略处理
        String processed = sensitiveWordService.process(content);
    }
}
```

### 4. 使用注解

```java
@RestController
@RequestMapping("/api/content")
public class ContentController {
    
    // 方法级注解 - 检测所有字符串参数
    @PostMapping("/check")
    @SensitiveWordCheck(handleType = HandleType.EXCEPTION)
    public Result<String> checkContent(@RequestBody String content) {
        return Result.success("内容合法");
    }
    
    // 方法级注解 - 检测对象指定字段
    @PostMapping("/submit")
    @SensitiveWordCheck(fields = {"title", "content"}, handleType = HandleType.REPLACE)
    public Result<Article> submitArticle(@RequestBody Article article) {
        return Result.success(article);
    }
    
    // 参数级注解
    @PostMapping("/comment")
    public Result<String> addComment(
            @SensitiveWordCheck(handleType = HandleType.REPLACE) @RequestBody String comment) {
        return Result.success(comment);
    }
}
```

### 5. 字段注解

```java
public class Article {
    
    private Long id;
    
    @SensitiveWordField(handleType = HandleType.REPLACE, replaceChar = '*')
    private String title;
    
    @SensitiveWordField(handleType = HandleType.REPLACE, matchType = MatchType.MAX_MATCH)
    private String content;
    
    private String author;
}
```

## 配置说明

### 基础配置

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `sensitive-word.enabled` | boolean | true | 是否启用敏感词过滤 |
| `sensitive-word.ignore-case` | boolean | true | 是否忽略大小写 |
| `sensitive-word.skip-whitespace` | boolean | true | 是否跳过空白字符 |
| `sensitive-word.skip-chars` | Set<Character> | 空 | 需要跳过的特殊字符 |
| `sensitive-word.match-type` | MatchType | MIN_MATCH | 默认匹配类型 |
| `sensitive-word.handle-type` | HandleType | REPLACE | 默认处理类型 |
| `sensitive-word.replace-char` | char | * | 替换字符 |
| `sensitive-word.replace-str` | String | - | 替换字符串（优先级高于 replace-char） |

### 词库配置

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `sensitive-word.words` | Set<String> | 空 | 直接配置的敏感词列表 |
| `sensitive-word.white-list` | Set<String> | 空 | 白名单词汇 |
| `sensitive-word.dict-paths` | List<String> | 空 | 内置词库路径（classpath） |
| `sensitive-word.external-dict-paths` | List<String> | 空 | 外部词库路径（文件系统） |

### 高亮配置

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `sensitive-word.highlight-start-tag` | String | `<span class="sensitive">` | 高亮开始标签 |
| `sensitive-word.highlight-end-tag` | String | `</span>` | 高亮结束标签 |

### 异常配置

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `sensitive-word.exception-message` | String | 内容包含敏感词：{} | 异常消息模板 |

## 匹配类型说明

### MIN_MATCH（最小匹配）

当词库中存在 "中国" 和 "中国人" 两个敏感词时，文本 "中国人民" 只会匹配到 "中国"。

### MAX_MATCH（最大匹配）

当词库中存在 "中国" 和 "中国人" 两个敏感词时，文本 "中国人民" 会匹配到 "中国人"。

## 处理类型说明

| 类型 | 说明 |
|------|------|
| `REPLACE` | 替换敏感词为指定字符 |
| `EXCEPTION` | 检测到敏感词时抛出 SensitiveWordException |
| `HIGHLIGHT` | 使用 HTML 标签包裹敏感词 |
| `DETECT_ONLY` | 仅检测并记录日志，不做处理 |

## 高级用法

### 动态管理敏感词

```java
@Service
@RequiredArgsConstructor
public class SensitiveWordManager {
    
    private final SensitiveWordService sensitiveWordService;
    
    // 添加敏感词
    public void addWord(String word) {
        sensitiveWordService.addWord(word);
    }
    
    // 添加敏感词（带分类）
    public void addWord(String word, String category) {
        sensitiveWordService.addWord(word, category);
    }
    
    // 批量添加
    public void addWords(Set<String> words) {
        sensitiveWordService.addWords(words);
    }
    
    // 移除敏感词
    public void removeWord(String word) {
        sensitiveWordService.removeWord(word);
    }
    
    // 添加白名单
    public void addWhiteList(String word) {
        sensitiveWordService.addWhiteList(word);
    }
    
    // 获取敏感词数量
    public int getWordCount() {
        return sensitiveWordService.size();
    }
}
```

### 使用工具类

```java
// 静态方法调用（不依赖 Spring 容器）
public class ContentChecker {
    
    public void check(String content) {
        // 检测
        boolean contains = SensitiveWordUtil.contains(content);
        
        // 查找
        List<SensitiveWordResult> results = SensitiveWordUtil.findAll(content);
        
        // 替换
        String filtered = SensitiveWordUtil.replace(content, '*');
        
        // 高亮
        String highlighted = SensitiveWordUtil.highlight(content, "<em>", "</em>");
    }
}
```

### 自定义过滤器

```java
@Configuration
public class CustomSensitiveWordConfig {
    
    @Bean
    public SensitiveWordFilter sensitiveWordFilter() {
        DfaSensitiveWordFilter filter = new DfaSensitiveWordFilter();
        filter.setIgnoreCase(true);
        filter.setSkipWhitespace(true);
        
        // 配置跳过的特殊字符
        Set<Character> skipChars = new HashSet<>();
        skipChars.add('@');
        skipChars.add('#');
        skipChars.add('$');
        filter.setSkipChars(skipChars);
        
        // 预加载敏感词
        filter.addWords(Set.of("敏感词1", "敏感词2"));
        
        return filter;
    }
}
```

### 异常处理

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(SensitiveWordException.class)
    public Result<Void> handleSensitiveWordException(SensitiveWordException e) {
        // 获取检测到的敏感词
        List<String> words = e.getAllSensitiveWords();
        
        return Result.fail("内容包含违规词汇: " + String.join(", ", words));
    }
}
```

## 词库文件格式

词库文件为纯文本格式，每行一个敏感词：

```text
# 这是注释行
敏感词1
敏感词2
敏感词3
# 支持空行

敏感词4
```

- 以 `#` 开头的行为注释
- 空行会被忽略
- 每行一个敏感词
- 使用 UTF-8 编码

## 性能说明

DFA 算法特点：

- **时间复杂度**：O(n)，n 为文本长度
- **空间复杂度**：O(m)，m 为敏感词总字符数
- **适用场景**：大量文本检测、实时内容过滤

## 注意事项

1. **词库加载顺序**：配置词汇 > 内置词库 > 外部词库
2. **白名单优先**：白名单词汇不会被检测为敏感词
3. **线程安全**：服务和过滤器都是线程安全的
4. **动态更新**：支持运行时动态添加/删除敏感词
5. **内存占用**：大量敏感词会占用一定内存（DFA 树结构）

## License

Apache License 2.0
