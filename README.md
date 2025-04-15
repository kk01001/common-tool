# Common Tool 工具集

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![JDK](https://img.shields.io/badge/JDK-21-green.svg)](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)

## 项目介绍

Common Tool 是一个面向 Java Spring Boot 应用的工具集合，提供多个开箱即用的 Starter 组件，旨在简化开发流程、提高代码质量和开发效率。
项目基于 Java 21 和 Spring Boot 3.x 构建，采用模块化设计，各组件可独立使用或组合使用。

## 模块列表

项目包含以下核心功能模块：

| 模块名称 | 最新版本 | 主要功能 |
| -------- | -------- | -------- |
| [common-tool-spring-boot-starter](#common-tool-spring-boot-starter) | [![Maven Central](https://img.shields.io/maven-central/v/io.github.kk01001/common-tool-spring-boot-starter.svg)](https://search.maven.org/search?q=g:io.github.kk01001%20a:common-tool-spring-boot-starter) | 通用工具集、分布式ID、国际化支持等 |
| [xxl-job-spring-boot-starter](#xxl-job-spring-boot-starter) | [![Maven Central](https://img.shields.io/maven-central/v/io.github.kk01001/xxl-job-spring-boot-starter.svg)](https://search.maven.org/search?q=g:io.github.kk01001%20a:xxl-job-spring-boot-starter) | XXL-JOB任务调度自动化集成 |
| [robot-message-spring-boot-starter](#robot-message-spring-boot-starter) | [![Maven Central](https://img.shields.io/maven-central/v/io.github.kk01001/robot-message-spring-boot-starter.svg)](https://search.maven.org/search?q=g:io.github.kk01001%20a:robot-message-spring-boot-starter) | 多平台消息通知组件 |
| [oss-spring-boot-starter](#oss-spring-boot-starter) | [![Maven Central](https://img.shields.io/maven-central/v/io.github.kk01001/oss-spring-boot-starter.svg)](https://search.maven.org/search?q=g:io.github.kk01001%20a:oss-spring-boot-starter) | 对象存储服务集成 |
| [script-spring-boot-starter](#script-spring-boot-starter) | [![Maven Central](https://img.shields.io/maven-central/v/io.github.kk01001/script-spring-boot-starter.svg)](https://search.maven.org/search?q=g:io.github.kk01001%20a:script-spring-boot-starter) | 多语言脚本执行支持 |
| [netty-spring-boot-starter](#netty-spring-boot-starter) | [![Maven Central](https://img.shields.io/maven-central/v/io.github.kk01001/netty-spring-boot-starter.svg)](https://search.maven.org/search?q=g:io.github.kk01001%20a:netty-spring-boot-starter) | Netty服务器与WebSocket支持 |
| [desensitize-spring-boot-starter](#desensitize-spring-boot-starter) | [![Maven Central](https://img.shields.io/maven-central/v/io.github.kk01001/desensitize-spring-boot-starter.svg)](https://search.maven.org/search?q=g:io.github.kk01001%20a:desensitize-spring-boot-starter) | 数据脱敏处理组件 |
| [multi-redis-spring-boot-starter](#multi-redis-spring-boot-starter) | [![Maven Central](https://img.shields.io/maven-central/v/io.github.kk01001/multi-redis-spring-boot-starter.svg)](https://search.maven.org/search?q=g:io.github.kk01001%20a:multi-redis-spring-boot-starter) | 多Redis源支持 |
| [mybatis-plus-spring3-boot-starter](#mybatis-plus-spring3-boot-starter) | [![Maven Central](https://img.shields.io/maven-central/v/io.github.kk01001/mybatis-plus-spring3-boot-starter.svg)](https://search.maven.org/search?q=g:io.github.kk01001%20a:mybatis-plus-spring3-boot-starter) | MyBatis-Plus与Spring Boot 3集成 |
| [sftp-spring-boot-starter](#sftp-spring-boot-starter) | [![Maven Central](https://img.shields.io/maven-central/v/io.github.kk01001/sftp-spring-boot-starter.svg)](https://search.maven.org/search?q=g:io.github.kk01001%20a:sftp-spring-boot-starter) | SFTP文件传输服务 |
| [dynamic-mq-spring-boot-starter](#dynamic-mq-spring-boot-starter) | [![Maven Central](https://img.shields.io/maven-central/v/io.github.kk01001/dynamic-mq-spring-boot-starter.svg)](https://search.maven.org/search?q=g:io.github.kk01001%20a:dynamic-mq-spring-boot-starter) | 动态消息队列管理 |
| [rate-limiter-spring-boot-starter](#rate-limiter-spring-boot-starter) | [![Maven Central](https://img.shields.io/maven-central/v/io.github.kk01001/rate-limiter-spring-boot-starter.svg)](https://search.maven.org/search?q=g:io.github.kk01001%20a:rate-limiter-spring-boot-starter) | 分布式限流组件 |
| [lock-spring-boot-starter](#lock-spring-boot-starter) | [![Maven Central](https://img.shields.io/maven-central/v/io.github.kk01001/lock-spring-boot-starter.svg)](https://search.maven.org/search?q=g:io.github.kk01001%20a:lock-spring-boot-starter) | 分布式锁实现 |
| [idempotent-spring-boot-starter](#idempotent-spring-boot-starter) | [![Maven Central](https://img.shields.io/maven-central/v/io.github.kk01001/idempotent-spring-boot-starter.svg)](https://search.maven.org/search?q=g:io.github.kk01001%20a:idempotent-spring-boot-starter) | 接口幂等性保障 |
| [ip2region-spring-boot-starter](#ip2region-spring-boot-starter) | [![Maven Central](https://img.shields.io/maven-central/v/io.github.kk01001/ip2region-spring-boot-starter.svg)](https://search.maven.org/search?q=g:io.github.kk01001%20a:ip2region-spring-boot-starter) | IP地址归属地查询 |
| [docs-spring-boot-starter](#docs-spring-boot-starter) | [![Maven Central](https://img.shields.io/maven-central/v/io.github.kk01001/docs-spring-boot-starter.svg)](https://search.maven.org/search?q=g:io.github.kk01001%20a:docs-spring-boot-starter) | API文档自动生成 |
| [excel-spring-boot-starter](#excel-spring-boot-starter) | [![Maven Central](https://img.shields.io/maven-central/v/io.github.kk01001/excel-spring-boot-starter.svg)](https://search.maven.org/search?q=g:io.github.kk01001%20a:excel-spring-boot-starter) | Excel导入导出处理 |
| [crypto-spring-boot-starter](#crypto-spring-boot-starter) | [![Maven Central](https://img.shields.io/maven-central/v/io.github.kk01001/crypto-spring-boot-starter.svg)](https://search.maven.org/search?q=g:io.github.kk01001%20a:crypto-spring-boot-starter) | 加密解密工具集成 |
| [disruptor-spring-boot-starter](#disruptor-spring-boot-starter) | [![Maven Central](https://img.shields.io/maven-central/v/io.github.kk01001/disruptor-spring-boot-starter.svg)](https://search.maven.org/search?q=g:io.github.kk01001%20a:disruptor-spring-boot-starter) | 高性能队列Disruptor集成 |
| [dict-spring-boot-starter](#dict-spring-boot-starter) | [![Maven Central](https://img.shields.io/maven-central/v/io.github.kk01001/dict-spring-boot-starter.svg)](https://search.maven.org/search?q=g:io.github.kk01001%20a:dict-spring-boot-starter) | 数据字典管理 |
| [design-pattern-spring-boot-starter](#design-pattern-spring-boot-starter) | [![Maven Central](https://img.shields.io/maven-central/v/io.github.kk01001/design-pattern-spring-boot-starter.svg)](https://search.maven.org/search?q=g:io.github.kk01001%20a:design-pattern-spring-boot-starter) | 设计模式工具集 |
| [local-cache-spring-boot-starter](#local-cache-spring-boot-starter) | [![Maven Central](https://img.shields.io/maven-central/v/io.github.kk01001/local-cache-spring-boot-starter.svg)](https://search.maven.org/search?q=g:io.github.kk01001%20a:local-cache-spring-boot-starter) | 本地缓存实现 |

## 详细介绍

### common-tool-spring-boot-starter

通用工具集合组件，提供一系列通用工具类和功能，简化日常开发。

#### 主要功能

- **国际化支持（i18n）**：灵活的多语言支持，支持基于内存和资源文件的消息管理
- **字符串工具**：丰富的字符串处理方法，包括格式化、转换和验证
- **日期时间工具**：简化日期时间操作，支持多种格式转换和计算
- **文件操作工具**：简化文件读写、复制、移动等操作
- **加密解密工具**：内置常用加密算法，如MD5、SHA、AES、RSA等
- **验证工具**：提供多种数据验证方法，如邮箱、手机号、身份证等
- **集合工具**：增强对List、Map等集合类型的操作
- **数字处理工具**：数值格式化、随机数生成、精度控制等功能

#### 使用示例

```java
// 国际化使用示例
@Service
public class UserService {
    @Autowired
    private I18nManager i18nManager;
    
    public void sendWelcomeMessage(String userId, Locale locale) {
        User user = userRepository.findById(userId);
        // 获取国际化消息
        String welcomeMsg = i18nManager.getMessage("user.welcome", locale, user.getName());
        notificationService.send(user, welcomeMsg);
    }
    
    // 动态添加国际化消息
    public void addCustomMessage() {
        Map<String, String> messages = new HashMap<>();
        messages.put("custom.message", "自定义消息");
        i18nManager.addMessages(Locale.SIMPLIFIED_CHINESE, messages);
    }
}

// 字符串工具示例
@RestController
@RequestMapping("/api/tools")
public class ToolController {
    
    @GetMapping("/string/mask")
    public String maskString(@RequestParam String value) {
        // 对字符串进行掩码处理
        return StringUtil.mask(value, 3, 4, '*');  // 例如：138****1234
    }
    
    @GetMapping("/encrypt")
    public String encrypt(@RequestParam String text, @RequestParam String key) {
        // AES加密
        return EncryptUtil.aesEncrypt(text, key);
    }
    
    @GetMapping("/decrypt")
    public String decrypt(@RequestParam String encryptedText, @RequestParam String key) {
        // AES解密
        return EncryptUtil.aesDecrypt(encryptedText, key);
    }
}
```
[查看详细文档](./common-tool-spring-boot-starter/README.md)

### xxl-job-spring-boot-starter

基于XXL-JOB的分布式任务调度组件，提供任务的自动注册功能。

#### 主要功能

- **执行器自动注册**：启动时自动向XXL-JOB调度中心注册当前应用为执行器
- **任务自动注册**：通过注解自动注册任务到XXL-JOB调度中心
- **简化配置**：提供简洁的配置方式，降低接入成本
- **完整参数支持**：支持XXL-JOB的所有调度参数配置
- **分片任务支持**：提供便捷的分片任务工具类

#### 使用示例

```java
@XxlJob("demoJobHandler")
@XxlJobRegister(
    cron = "0 0 1 * * ?",
    jobDesc = "示例任务",
    author = "admin",
    triggerStatus = 1,
    executorRouteStrategy = ExecutorRouteStrategyEnum.ROUND
)
public void demoJobHandler() {
    log.info("XXL-JOB任务执行");
}
```

[查看详细文档](./xxl-job-spring-boot-starter/README.md)

### robot-message-spring-boot-starter

多平台消息通知组件，支持钉钉、企业微信、邮件、短信等多种通知方式。

#### 主要功能

- **多平台支持**：集成钉钉、企业微信、邮件、短信等多种消息通知平台
- **统一API**：提供统一的消息发送接口，简化多平台对接
- **丰富消息类型**：支持文本、Markdown、链接、图片等多种消息格式
- **可扩展架构**：基于SPI机制，方便扩展新的消息平台
- **配置热更新**：支持配置的动态更新，无需重启应用
- **脚本支持**：支持Groovy脚本动态生成消息内容
- **并发处理**：优化的消息发送机制，提高处理效率

#### 使用示例

```java
@Autowired
private RobotClient robotClient;

// 发送文本消息
TextMessage textMessage = new TextMessage();
textMessage.setContent("测试消息");
robotClient.sendMessage(textMessage);

// 发送Markdown消息
MarkdownMessage markdownMessage = new MarkdownMessage();
markdownMessage.setTitle("通知");
markdownMessage.setContent("### 测试标题\n> 测试内容");
robotClient.sendMessage(markdownMessage);
```

[查看详细文档](./robot-message-spring-boot-starter/README.md)

### oss-spring-boot-starter

对象存储服务集成组件，提供统一的文件上传下载API，支持多种对象存储服务。

#### 主要功能

- **多供应商支持**：兼容AWS S3、阿里云OSS、腾讯云COS等多种对象存储服务
- **统一API**：提供统一的文件操作接口，屏蔽底层实现差异
- **简化操作**：封装常用的文件上传、下载、删除等操作，减少样板代码
- **分片上传**：支持大文件分片上传和断点续传
- **上传进度监控**：提供上传进度回调功能
- **丰富的配置选项**：支持配置存储桶、访问权限、过期时间等

#### 使用示例

```java
@Autowired
private OssClient ossClient;

// 上传文件
String objectName = ossClient.upload(file, "folder/file.jpg");

// 获取下载URL
String url = ossClient.getDownloadUrl("folder/file.jpg");

// 删除文件
ossClient.deleteObject("folder/file.jpg");
```

[查看详细文档](./oss-spring-boot-starter/README.md)

### script-spring-boot-starter

多语言脚本执行支持，提供在Java应用中动态执行脚本的能力。

#### 主要功能

- **多语言支持**：支持Groovy、JavaScript、Lua、Python等多种脚本语言
- **统一执行接口**：提供统一的脚本执行API，简化调用
- **脚本缓存**：支持脚本编译结果缓存，提高执行效率
- **执行超时控制**：防止脚本执行时间过长导致系统资源占用
- **安全沙箱**：限制脚本执行环境，防止恶意代码
- **错误处理**：提供完善的异常处理机制
- **数据交互**：支持Java与脚本之间的数据交互

#### 使用示例

```java
@Autowired
private ScriptService scriptService;

// 执行Groovy脚本
Map<String, Object> params = new HashMap<>();
params.put("name", "world");
Object result = scriptService.execute("return 'Hello, ' + name", params, ScriptType.GROOVY);

// 执行JavaScript脚本
String jsScript = "function add(a, b) { return a + b; }; add(x, y);";
Map<String, Object> jsParams = new HashMap<>();
jsParams.put("x", 10);
jsParams.put("y", 20);
Object jsResult = scriptService.execute(jsScript, jsParams, ScriptType.JAVASCRIPT);
```

[查看详细文档](./script-spring-boot-starter/README.md)

### netty-spring-boot-starter

Netty服务器与WebSocket支持组件，简化基于Netty的高性能WebSocket服务开发。

#### 主要功能

- **WebSocket服务器**：快速搭建基于Netty的WebSocket服务
- **注解驱动开发**：通过注解定义WebSocket端点和处理方法
- **会话管理**：内置WebSocket会话管理，支持会话属性存储
- **身份认证**：提供WebSocket连接认证机制
- **集群支持**：基于Redis的集群会话管理和消息广播
- **心跳检测**：自动心跳检测与连接维护
- **二进制消息**：支持文本和二进制消息处理

#### 使用示例

```java
@WebSocketEndpoint(path = "/ws/chat")
public class ChatEndpoint {

    @OnOpen
    public void onOpen(WebSocketSession session) {
        System.out.println("连接建立: " + session.getId());
    }

    @OnMessage
    public void onMessage(WebSocketSession session, String message) {
        // 处理接收到的消息
        System.out.println("收到消息: " + message);
        
        // 发送消息
        session.sendText("服务器已收到消息: " + message);
    }

    @OnClose
    public void onClose(WebSocketSession session) {
        System.out.println("连接关闭: " + session.getId());
    }
}
```

[查看详细文档](./netty-spring-boot-starter/README.md)

### desensitize-spring-boot-starter

数据脱敏处理组件，支持多种脱敏场景和自定义脱敏规则。

#### 主要功能

- **多场景支持**：支持手机号、邮箱、身份证、银行卡等多种类型数据脱敏
- **多框架集成**：无缝集成Jackson、FastJson等主流JSON框架
- **注解驱动**：通过简单注解实现字段级脱敏
- **灵活配置**：支持全局和定制化脱敏规则配置
- **条件脱敏**：支持基于条件的动态脱敏处理
- **可扩展性**：支持自定义脱敏处理器和规则
- **高性能**：高效的脱敏算法，最小化性能影响

#### 使用示例

```java
// 数据模型定义
public class UserDTO {
    private String name;
    
    @Desensitize(strategy = DesensitizeStrategy.PHONE)
    private String phone;
    
    @Desensitize(strategy = DesensitizeStrategy.EMAIL)
    private String email;
    
    @Desensitize(strategy = DesensitizeStrategy.ID_CARD)
    private String idCard;
    
    @Desensitize(strategy = DesensitizeStrategy.CUSTOM, 
                handler = "bankCardHandler",
                keepPrefix = 4, keepSuffix = 4)
    private String bankCard;
    
    // getter和setter
}

// 控制器使用
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable Long id) {
        UserDTO user = userService.getUser(id);
        // 返回时自动对标记的字段进行脱敏
        return ResponseEntity.ok(user);
    }
}
```

[查看详细文档](./desensitize-spring-boot-starter/README.md)

### multi-redis-spring-boot-starter

多Redis数据源支持组件，用于在单个应用中连接和管理多个Redis实例。

#### 主要功能

- **多数据源配置**：支持配置多个Redis连接并自动注入
- **动态切换**：运行时动态切换Redis实例
- **连接池管理**：优化的连接池配置与监控
- **自动路由**：基于注解或方法名称的自动数据源路由
- **操作模板复用**：在不同数据源间复用RedisTemplate实例
- **集群支持**：兼容Redis单机、集群和哨兵模式

#### 使用示例

```java
// 配置多个Redis源
@Configuration
public class RedisConfig {
    @Primary
    @Bean
    public RedisTemplate<String, Object> primaryRedisTemplate(
            @Qualifier("primaryRedisConnectionFactory") RedisConnectionFactory factory) {
        return createRedisTemplate(factory);
    }
    
    @Bean
    public RedisTemplate<String, Object> secondaryRedisTemplate(
            @Qualifier("secondaryRedisConnectionFactory") RedisConnectionFactory factory) {
        return createRedisTemplate(factory);
    }
}

// 使用特定的Redis源
@Service
public class UserService {
    @Autowired
    @Qualifier("primaryRedisTemplate")
    private RedisTemplate<String, Object> primaryRedis;
    
    @Autowired
    @Qualifier("secondaryRedisTemplate")
    private RedisTemplate<String, Object> secondaryRedis;
    
    public void saveToMultipleRedis(User user) {
        // 写入主Redis
        primaryRedis.opsForValue().set("user:" + user.getId(), user);
        
        // 写入从Redis
        secondaryRedis.opsForValue().set("user:" + user.getId(), user);
    }
}
```

[查看详细文档](./multi-redis-spring-boot-starter/README.md)

### mybatis-plus-spring3-boot-starter

MyBatis-Plus与Spring Boot 3集成组件，提供增强的ORM和CRUD操作支持。

#### 主要功能

- **无侵入增强**：在MyBatis基础上增强，不做任何侵入修改
- **Spring Boot 3适配**：完全适配Spring Boot 3和Jakarta EE规范
- **CRUD操作**：内置通用Mapper和Service，实现单表大部分CRUD操作
- **条件构造器**：强大的条件构造器，满足各类查询需求
- **分页插件**：内置分页插件，自动优化分页SQL
- **逻辑删除**：支持逻辑删除，保护数据安全
- **自动填充**：字段自动填充，节省重复代码

#### 使用示例

```java
@Data
@TableName("sys_user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String username;
    
    private String password;
    
    @TableLogic
    private Integer deleted;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

@Service
public class UserService extends ServiceImpl<UserMapper, User> {
    
    public List<User> findActiveUsers() {
        return this.lambdaQuery()
                .eq(User::getDeleted, 0)
                .like(User::getUsername, "admin")
                .orderByDesc(User::getCreateTime)
                .list();
    }
}
```

[查看详细文档](./mybatis-plus-spring3-boot-starter/README.md)

### sftp-spring-boot-starter

SFTP文件传输服务组件，提供安全、高效的文件传输功能。

#### 主要功能

- **自动连接管理**：自动建立和释放SFTP连接
- **连接池支持**：内置连接池，提高传输效率
- **并发传输**：支持并发上传和下载
- **断点续传**：支持大文件分片上传与断点续传
- **路径配置**：灵活的路径配置和目录管理
- **安全传输**：基于SSH的安全传输机制
- **传输进度监控**：提供传输进度回调

#### 使用示例

```java
@Autowired
private SftpClient sftpClient;

// 上传文件
public void uploadFile() {
    File file = new File("/local/path/file.txt");
    String remoteDir = "/remote/path/";
    sftpClient.uploadFile(file, remoteDir);
}

// 下载文件
public void downloadFile() {
    String remoteFilePath = "/remote/path/file.txt";
    String localDir = "/local/path/";
    sftpClient.downloadFile(remoteFilePath, localDir);
}

// 列出远程目录文件
public List<String> listFiles() {
    String remoteDir = "/remote/path/";
    return sftpClient.listFiles(remoteDir);
}
```

[查看详细文档](./sftp-spring-boot-starter/README.md)

### dynamic-mq-spring-boot-starter

动态消息队列管理组件，支持多种消息队列平台的统一接入和动态配置。

#### 主要功能

- **多平台支持**：支持Kafka、RabbitMQ、RocketMQ等多种消息队列
- **统一API**：提供统一的生产和消费接口
- **动态配置**：支持运行时动态修改队列配置
- **自动重连**：连接异常自动重连机制
- **消息追踪**：支持消息生产和消费全链路追踪
- **批量处理**：高效的批量消息处理
- **错误处理**：完善的异常处理和重试机制

#### 使用示例

```java
@Autowired
private MqTemplate mqTemplate;

// 发送消息
public void sendMessage() {
    String topic = "order-topic";
    String message = "订单已创建: 123456";
    mqTemplate.send(topic, message);
}

// 消费消息
@MqListener(topic = "order-topic")
public void receiveMessage(String message) {
    System.out.println("收到消息: " + message);
}

// 批量发送
public void batchSend() {
    String topic = "batch-topic";
    List<String> messages = Arrays.asList("消息1", "消息2", "消息3");
    mqTemplate.batchSend(topic, messages);
}
```

[查看详细文档](./dynamic-mq-spring-boot-starter/README.md)

### rate-limiter-spring-boot-starter

分布式限流组件，提供多种限流策略和易用的注解配置。

#### 主要功能

- **多种限流算法**：支持令牌桶、漏桶、窗口计数等限流算法
- **注解驱动**：通过简单注解实现方法级限流
- **分布式支持**：基于Redis的分布式限流实现
- **灵活配置**：支持全局和细粒度限流配置
- **降级处理**：提供限流触发后的降级处理机制
- **监控统计**：限流指标监控和统计
- **按条件限流**：支持按IP、用户ID等维度进行限流

#### 使用示例

```java
@Service
public class OrderService {
    
    @RateLimit(key = "createOrder", limit = 10, period = 1, unit = TimeUnit.MINUTES)
    public void createOrder(OrderDTO order) {
        // 创建订单逻辑
    }
    
    @RateLimit(key = "queryOrder", limit = 50, period = 1, unit = TimeUnit.SECONDS)
    public OrderDTO getOrder(String orderId) {
        // 查询订单逻辑
        return new OrderDTO();
    }
}
```

配置:

```yaml
rate-limiter:
  enabled: true
  type: redis  # 或 local
  fail-strategy: exception  # exception, discard 或 custom
```

[查看详细文档](./rate-limiter-spring-boot-starter/README.md)

### lock-spring-boot-starter

分布式锁实现组件，提供基于Redis或Zookeeper的分布式锁。

#### 主要功能

- **多种实现**：支持Redis、Zookeeper等多种分布式锁实现
- **注解支持**：通过注解轻松添加分布式锁
- **锁超时与续期**：自动管理锁超时和续期
- **可重入锁**：支持可重入锁机制
- **读写锁**：支持读写分离锁
- **尝试锁**：支持尝试获取锁与等待超时
- **锁释放保障**：确保锁在异常情况下也能正确释放

#### 使用示例

```java
@Service
public class InventoryService {
    
    @DistributedLock(key = "inventory:#{#productId}", timeout = 10, timeUnit = TimeUnit.SECONDS)
    public boolean reduceStock(String productId, int quantity) {
        // 减库存操作
        return true;
    }
    
    @DistributedLock(key = "order:create:#{#order.userId}", waitTime = 1000)
    public void createOrder(Order order) {
        // 创建订单逻辑
    }
}
```

[查看详细文档](./lock-spring-boot-starter/README.md)

### idempotent-spring-boot-starter

接口幂等性保障组件，防止重复请求和重复处理。

#### 主要功能

- **自动去重**：自动识别和拦截重复请求
- **多种策略**：支持基于Token、参数、ID等多种幂等策略
- **注解驱动**：通过注解简单实现幂等控制
- **过期机制**：可配置的幂等记录过期时间
- **分布式支持**：基于Redis的分布式幂等实现
- **适用场景广**：适用于API接口、消息处理等多种场景
- **自定义扩展**：支持自定义幂等键生成策略

#### 使用示例

```java
@RestController
@RequestMapping("/orders")
public class OrderController {
    
    @PostMapping("/create")
    @Idempotent(key = "create:order:#{#request.orderNo}", expireTime = 5, timeUnit = TimeUnit.MINUTES)
    public ResponseEntity<Order> createOrder(@RequestBody OrderRequest request) {
        // 创建订单逻辑
        return ResponseEntity.ok(orderService.create(request));
    }
    
    @PostMapping("/pay")
    @Idempotent(type = IdempotentType.TOKEN, tokenHeader = "Idempotent-Token")
    public ResponseEntity<PaymentResult> payOrder(@RequestBody PaymentRequest request) {
        // 支付处理逻辑
        return ResponseEntity.ok(paymentService.processPayment(request));
    }
}
```

[查看详细文档](./idempotent-spring-boot-starter/README.md)

### ip2region-spring-boot-starter

IP地址归属地查询组件，快速准确地查询IP地址的地理位置信息。

#### 主要功能

- **快速查询**：毫秒级IP地址归属地查询
- **内存优化**：多种查询算法，适应不同内存需求
- **准确度高**：基于最新IP地址库，提供精确查询结果
- **自动更新**：支持IP库的自动更新
- **多语言结果**：支持多语言查询结果
- **缓存机制**：内置缓存机制，提高查询效率
- **离线查询**：完全离线查询，不依赖外部服务

#### 使用示例

```java
@Autowired
private Ip2RegionSearcher searcher;

// 查询IP归属地
public void queryIpInfo(String ip) {
    IpInfo ipInfo = searcher.search(ip);
    System.out.println("国家: " + ipInfo.getCountry());
    System.out.println("省份: " + ipInfo.getProvince());
    System.out.println("城市: " + ipInfo.getCity());
    System.out.println("运营商: " + ipInfo.getIsp());
}

// 批量查询
public void batchQuery(List<String> ipList) {
    List<IpInfo> results = searcher.batchSearch(ipList);
    results.forEach(System.out::println);
}
```

[查看详细文档](./ip2region-spring-boot-starter/README.md)

### docs-spring-boot-starter

API文档自动生成组件，支持多种文档格式和样式定制。

#### 主要功能

- **接口扫描**：自动扫描并记录API接口
- **多格式支持**：支持Swagger、OpenAPI、Markdown等多种文档格式
- **丰富注解**：丰富的文档注解，详细描述API信息
- **分组管理**：API分组管理和权限控制
- **在线调试**：支持在线接口调试功能
- **文档导出**：支持导出为多种格式的离线文档
- **自定义主题**：文档界面自定义和主题设置

#### 使用示例

```java
@RestController
@RequestMapping("/api/users")
@ApiDoc(group = "用户管理", description = "用户相关接口")
public class UserController {
    
    @GetMapping("/{id}")
    @ApiDocMethod(summary = "获取用户信息", description = "根据用户ID获取用户详细信息")
    @ApiDocResponse("用户信息对象")
    public ResponseEntity<User> getUser(
            @ApiDocParam(name = "id", description = "用户ID") @PathVariable Long id) {
        return ResponseEntity.ok(userService.getUser(id));
    }
    
    @PostMapping
    @ApiDocMethod(summary = "创建用户", description = "创建新用户并返回用户信息")
    public ResponseEntity<User> createUser(@RequestBody UserCreateRequest request) {
        return ResponseEntity.ok(userService.createUser(request));
    }
}
```

[查看详细文档](./docs-spring-boot-starter/README.md)

## 技术栈

- Java 21
- Spring Boot 3.x
- Hutool 工具集
- Jakarta Validation API
- Jackson
- 各种特定领域组件集成

## 如何选择合适的组件

- 如果需要**基础工具支持**（如分布式ID、国际化等），选择 **common-tool-spring-boot-starter**
- 如果需要**集成分布式任务调度**，选择 **xxl-job-spring-boot-starter**
- 如果需要**多平台消息通知**功能，选择 **robot-message-spring-boot-starter**
- 如果需要**对象存储服务**集成，选择 **oss-spring-boot-starter**
- 如果需要**脚本执行**能力，选择 **script-spring-boot-starter**

## 环境要求

- JDK 21+
- Spring Boot 3.x
- Maven 3.6+

## 贡献指南

我们欢迎各种形式的贡献，包括功能建议、问题报告和代码贡献。

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/amazing-feature`)
3. 提交更改 (`git commit -m 'Add amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 创建Pull Request

## 许可证

Apache License 2.0
