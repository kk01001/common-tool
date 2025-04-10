# Common Tool Spring Boot Starter

[![Maven Central](https://img.shields.io/maven-central/v/io.github.kk01001/common-tool-spring-boot-starter.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:io.github.kk01001%20a:common-tool-spring-boot-starter)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

## 项目概述

`common-tool-spring-boot-starter` 是一个集成了多种常用工具和功能的 Spring Boot Starter，旨在简化 Spring Boot 应用的开发，提供开箱即用的通用工具集。包含分布式ID生成、国际化支持、网络工具、责任链模式实现等多种功能组件。

## 功能特性

- **分布式ID生成**：基于雪花算法的高性能分布式ID生成工具
- **国际化支持（i18n）**：灵活的国际化消息解析和管理
- **网络工具**：IP获取、系统属性获取等网络相关工具
- **请求链路追踪**：TraceId生成和管理，支持分布式系统的请求追踪
- **数据校验**：集成Jakarta Validation的便捷数据验证工具
- **Jackson工具**：JSON序列化与反序列化工具
- **责任链模式**：提供责任链模式的抽象实现
- **版本和Git信息**：内置版本信息和Git仓库信息查询接口

## 技术栈

- Java 21
- Spring Boot 3.x
- Hutool 工具集
- Jakarta Validation API
- Jackson

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>io.github.kk01001</groupId>
    <artifactId>common-tool-spring-boot-starter</artifactId>
    <version>${latest.version}</version>
</dependency>
```

### 2. 自动配置

依赖添加后，Spring Boot 会自动配置以下组件：

- ApplicationInfoInitialize - 应用信息初始化器
- IdWorkerUtil - 分布式ID生成器
- I18n相关组件（如果启用）
- 内置控制器（VersionController, GitInfoController）

## 核心功能详解

### 1. 分布式ID生成

基于雪花算法实现的分布式ID生成器，可用于生成全局唯一的ID。

```java
@Autowired
private IdWorkerUtil idWorkerUtil;

// 生成数值类型ID
Long id = idWorkerUtil.nextId();

// 生成字符串类型ID
String idStr = idWorkerUtil.nextIdStr();
```

### 2. 国际化支持

提供了灵活的国际化支持，支持基于资源文件和内存两种方式的消息存储。

#### 配置

```yaml
i18n:
  enabled: true  # 是否启用国际化
  default-locale: zh_CN  # 默认语言
  basename: i18n/messages  # 国际化资源文件路径
  cache-seconds: 3600  # 消息缓存过期时间(秒)
  always-use-message-format: false  # 是否总是使用消息格式化
  use-code-as-default-message: true  # 是否使用代码作为默认消息
  provider: resource  # 消息提供者类型: memory-内存, resource-资源文件
```

#### 使用示例

```java
@Autowired
private I18nService i18nService;

// 获取当前语言的消息
String message = i18nService.getMessage("message.code");

// 带参数的消息
String messageWithArgs = i18nService.getMessage("message.with.args", "arg1", "arg2");

// 获取指定语言的消息
String enMessage = i18nService.getMessageByLocale("message.code", "en_US");
```

#### 动态管理国际化消息(内存模式)

```java
@Autowired
private I18nManager i18nManager;

// 添加或更新消息
i18nManager.addMessage("zh_CN", "message.code", "消息内容");

// 批量添加或更新消息
Map<String, String> messages = new HashMap<>();
messages.put("message.code1", "消息内容1");
messages.put("message.code2", "消息内容2");
i18nManager.addMessages("zh_CN", messages);

// 移除消息
i18nManager.removeMessage("zh_CN", "message.code");

// 刷新消息缓存
i18nManager.refresh();
```

### 3. 请求链路追踪

提供了TraceId的生成和管理功能，用于跟踪请求链路。

```java
// 生成TraceId
String traceId = TraceIdUtil.generateTraceId();

// 设置TraceId到当前线程
TraceIdUtil.setTraceId(traceId);

// 获取当前线程的TraceId
String currentTraceId = TraceIdUtil.getTraceId();

// 构建并设置TraceId
TraceIdUtil.buildAndSetTraceId("component1", "operation1", "requestId123");

// 清除TraceId
TraceIdUtil.remove();
```

### 4. 数据校验

集成Jakarta Validation，提供便捷的数据校验方法。

```java
// 校验整个对象
ValidationUtil.validate(object);

// 校验对象的特定字段
ValidationUtil.validate(object, "fieldName");

// 按分组校验
ValidationUtil.validate(object, GroupA.class, GroupB.class);
```

### 5. 网络工具

提供了网络相关的工具方法。

```java
// 获取本地IP
String localIp = NetworkUtil.LOCAL_SERVER_IP;

// 获取系统属性
String property = NetworkUtil.getSystemProperty("property.key");

// 获取客户端IP
String clientIp = NetworkUtil.getClientIp(request);
```

### 6. 责任链模式

提供了责任链模式的抽象实现，方便构建复杂的业务处理流程。

```java
// 创建具体的处理器
public class Handler1 extends AbstractChainHandler<Context> {
    @Override
    public void doHandler(Context context) {
        // 处理逻辑
        
        // 继续下一个处理器
        nextHandler(context);
    }
}

// 构建责任链
AbstractChainHandler.Builder<Context> builder = new AbstractChainHandler.Builder<>();
builder.addHandler(new Handler1());
builder.addHandler(new Handler2());
builder.addHandler(new Handler3());

// 获取责任链
AbstractChainHandler<Context> chain = builder.build();

// 执行责任链
chain.doHandler(new Context());
```

### 7. 内置Controller

- `VersionController`：提供了获取应用版本信息的接口（`/get-version`）和健康检查接口（`/ping`）
- `GitInfoController`：提供了获取Git仓库信息的接口（`/git-info`）

## 应用信息初始化

当应用启动完成后，`ApplicationInfoInitialize` 会打印应用信息，包括：

- 版本信息访问URL
- 健康检查URL
- Prometheus指标URL
- API文档URL
- Nacos服务器配置

## 最佳实践

1. **分布式ID生成**：对于需要生成全局唯一ID的场景，使用`IdWorkerUtil`而不是UUID或数据库自增ID，以获得更高的性能和更好的分布式支持。

2. **国际化**：对于国际化需求，优先使用资源文件模式，只有在需要动态管理国际化消息时才使用内存模式。

3. **链路追踪**：在微服务架构中，确保所有服务都在处理请求时传递和维护TraceId，以便于问题排查。

4. **责任链模式**：对于复杂的业务流程处理，考虑使用责任链模式进行解耦。

## 注意事项

1. 该Starter仅支持Spring Boot 3.x版本，Java 21或更高版本。

2. 内置的Controller可能与应用中已有的URL路径冲突，如有需要可以通过Spring的映射优先级机制覆盖。

3. 确保应用中存在`version.json`文件以支持版本信息接口，以及`git.properties`文件以支持Git信息接口。

## 许可证

Apache License 2.0 