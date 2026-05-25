# Common Tool 工具集

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![JDK](https://img.shields.io/badge/JDK-21-green.svg)](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)

## 项目介绍

Common Tool 是一个面向 Java Spring Boot 应用的工具集合，提供多个开箱即用的 Starter 组件，旨在简化开发流程、提高代码质量和开发效率。
项目基于 Java 21 和 Spring Boot 3.x 构建，采用模块化设计，各组件可独立使用或组合使用。

## 目录

- [Common Tool 工具集](#common-tool-工具集)
  - [项目介绍](#项目介绍)
  - [目录](#目录)
  - [模块列表](#模块列表)
  - [详细介绍](#详细介绍)
    - [基础工具](#基础工具)
      - [common-tool-spring-boot-starter](#common-tool-spring-boot-starter)
        - [主要功能](#主要功能)
        - [使用示例](#使用示例)
      - [i18n-spring-boot-starter](#i18n-spring-boot-starter)
        - [主要功能](#主要功能-1)
        - [使用示例](#使用示例-1)
      - [design-pattern-spring-boot-starter](#design-pattern-spring-boot-starter)
        - [主要功能](#主要功能-2)
        - [使用示例](#使用示例-2)
    - [缓存与数据源](#缓存与数据源)
      - [multi-redis-spring-boot-starter](#multi-redis-spring-boot-starter)
        - [主要功能](#主要功能-3)
        - [使用示例](#使用示例-3)
      - [multi-redisson-spring-boot-starter](#multi-redisson-spring-boot-starter)
        - [主要功能](#主要功能-4)
        - [使用示例](#使用示例-4)
      - [local-cache-spring-boot-starter](#local-cache-spring-boot-starter)
        - [主要功能](#主要功能-5)
        - [使用示例](#使用示例-5)
      - [mybatis-plus-spring3-boot-starter](#mybatis-plus-spring3-boot-starter)
        - [主要功能](#主要功能-6)
        - [使用示例](#使用示例-6)
    - [消息通信](#消息通信)
      - [dynamic-mq-spring-boot-starter](#dynamic-mq-spring-boot-starter)
        - [主要功能](#主要功能-7)
        - [使用示例](#使用示例-7)
      - [mqtt-spring-boot-starter](#mqtt-spring-boot-starter)
        - [主要功能](#主要功能-8)
        - [使用示例](#使用示例-8)
      - [robot-message-spring-boot-starter](#robot-message-spring-boot-starter)
        - [主要功能](#主要功能-9)
        - [使用示例](#使用示例-9)
      - [push-spring-boot-starter](#push-spring-boot-starter)
        - [主要功能](#主要功能-10)
        - [使用示例](#使用示例-10)
      - [netty-spring-boot-starter](#netty-spring-boot-starter)
        - [主要功能](#主要功能-11)
        - [使用示例](#使用示例-11)
    - [分布式组件](#分布式组件)
      - [resilience4j-spring-boot-starter](#resilience4j-spring-boot-starter)
        - [主要功能](#主要功能-12)
        - [使用示例](#使用示例-12)
        - [配置示例](#配置示例)
      - [lock-spring-boot-starter](#lock-spring-boot-starter)
        - [主要功能](#主要功能-13)
        - [使用示例](#使用示例-13)
      - [rate-limiter-spring-boot-starter](#rate-limiter-spring-boot-starter)
        - [主要功能](#主要功能-14)
        - [使用示例](#使用示例-14)
      - [idempotent-spring-boot-starter](#idempotent-spring-boot-starter)
        - [主要功能](#主要功能-15)
        - [使用示例](#使用示例-15)
      - [local-message-spring-boot-starter](#local-message-spring-boot-starter)
        - [主要功能](#主要功能-16)
        - [使用示例](#使用示例-16)
    - [任务调度与线程池](#任务调度与线程池)
      - [xxl-job-spring-boot-starter](#xxl-job-spring-boot-starter)
        - [主要功能](#主要功能-17)
        - [使用示例](#使用示例-17)
      - [dynamic-threadpool-spring-boot-starter](#dynamic-threadpool-spring-boot-starter)
        - [主要功能](#主要功能-18)
        - [使用示例](#使用示例-18)
      - [disruptor-spring-boot-starter](#disruptor-spring-boot-starter)
        - [主要功能](#主要功能-19)
        - [使用示例](#使用示例-19)
    - [文件与存储](#文件与存储)
      - [oss-spring-boot-starter](#oss-spring-boot-starter)
        - [主要功能](#主要功能-20)
        - [使用示例](#使用示例-20)
      - [sftp-spring-boot-starter](#sftp-spring-boot-starter)
        - [主要功能](#主要功能-21)
        - [使用示例](#使用示例-21)
      - [excel-spring-boot-starter](#excel-spring-boot-starter)
        - [主要功能](#主要功能-22)
        - [使用示例](#使用示例-22)
    - [数据处理](#数据处理)
      - [desensitize-spring-boot-starter](#desensitize-spring-boot-starter)
        - [主要功能](#主要功能-23)
        - [使用示例](#使用示例-23)
      - [crypto-spring-boot-starter](#crypto-spring-boot-starter)
        - [主要功能](#主要功能-24)
        - [使用示例](#使用示例-24)
      - [dict-spring-boot-starter](#dict-spring-boot-starter)
        - [主要功能](#主要功能-25)
        - [使用示例](#使用示例-25)
      - [ip2region-spring-boot-starter](#ip2region-spring-boot-starter)
        - [主要功能](#主要功能-26)
        - [使用示例](#使用示例-26)
      - [sensitive-word-spring-boot-starter](#sensitive-word-spring-boot-starter)
        - [主要功能](#主要功能-27)
        - [使用示例](#使用示例-27)
      - [signature-spring-boot-starter](#signature-spring-boot-starter)
        - [主要功能](#主要功能-28)
        - [使用示例](#使用示例-28)
    - [脚本与扩展](#脚本与扩展)
      - [script-spring-boot-starter](#script-spring-boot-starter)
        - [主要功能](#主要功能-29)
        - [使用示例](#使用示例-29)
      - [ffmpeg-spring-boot-starter](#ffmpeg-spring-boot-starter)
        - [主要功能](#主要功能-30)
        - [使用示例](#使用示例-30)
    - [接口文档](#接口文档)
      - [docs-spring-boot-starter](#docs-spring-boot-starter)
        - [主要功能](#主要功能-31)
        - [使用示例](#使用示例-31)
  - [技术栈](#技术栈)
  - [如何选择合适的组件](#如何选择合适的组件)
  - [环境要求](#环境要求)
  - [贡献指南](#贡献指南)
  - [许可证](#许可证)

## 模块列表

项目包含以下核心功能模块：

| 模块名称                                                                          | 最新版本                                                                                                                                                                                                                  | 主要功能                        |
| --------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------- |
| [common-tool-spring-boot-starter](#common-tool-spring-boot-starter)               | [![Maven Central](https://img.shields.io/maven-central/v/io.github.archer099/common-tool-spring-boot-starter.svg)](https://search.maven.org/search?q=g:io.github.archer099%20a:common-tool-spring-boot-starter)               | 通用工具集、分布式ID等          |
| [i18n-spring-boot-starter](#i18n-spring-boot-starter)                             | [![Maven Central](https://img.shields.io/maven-central/v/io.github.archer099/i18n-spring-boot-starter.svg)](https://search.maven.org/search?q=g:io.github.archer099%20a:i18n-spring-boot-starter)                             | 国际化支持                      |
| [xxl-job-spring-boot-starter](#xxl-job-spring-boot-starter)                       | [![Maven Central](https://img.shields.io/maven-central/v/io.github.archer099/xxl-job-spring-boot-starter.svg)](https://search.maven.org/search?q=g:io.github.archer099%20a:xxl-job-spring-boot-starter)                       | XXL-JOB任务调度自动化集成       |
| [robot-message-spring-boot-starter](#robot-message-spring-boot-starter)           | [![Maven Central](https://img.shields.io/maven-central/v/io.github.archer099/robot-message-spring-boot-starter.svg)](https://search.maven.org/search?q=g:io.github.archer099%20a:robot-message-spring-boot-starter)           | 多平台机器人消息通知组件        |
| [push-spring-boot-starter](#push-spring-boot-starter)                             | [![Maven Central](https://img.shields.io/maven-central/v/io.github.archer099/push-spring-boot-starter.svg)](https://search.maven.org/search?q=g:io.github.archer099%20a:push-spring-boot-starter)                             | 移动端推送服务                  |
| [oss-spring-boot-starter](#oss-spring-boot-starter)                               | [![Maven Central](https://img.shields.io/maven-central/v/io.github.archer099/oss-spring-boot-starter.svg)](https://search.maven.org/search?q=g:io.github.archer099%20a:oss-spring-boot-starter)                               | 对象存储服务集成                |
| [script-spring-boot-starter](#script-spring-boot-starter)                         | [![Maven Central](https://img.shields.io/maven-central/v/io.github.archer099/script-spring-boot-starter.svg)](https://search.maven.org/search?q=g:io.github.archer099%20a:script-spring-boot-starter)                         | 多语言脚本执行支持              |
| [netty-spring-boot-starter](#netty-spring-boot-starter)                           | [![Maven Central](https://img.shields.io/maven-central/v/io.github.archer099/netty-spring-boot-starter.svg)](https://search.maven.org/search?q=g:io.github.archer099%20a:netty-spring-boot-starter)                           | Netty服务器与WebSocket支持      |
| [desensitize-spring-boot-starter](#desensitize-spring-boot-starter)               | [![Maven Central](https://img.shields.io/maven-central/v/io.github.archer099/desensitize-spring-boot-starter.svg)](https://search.maven.org/search?q=g:io.github.archer099%20a:desensitize-spring-boot-starter)               | 数据脱敏处理组件                |
| [multi-redis-spring-boot-starter](#multi-redis-spring-boot-starter)               | [![Maven Central](https://img.shields.io/maven-central/v/io.github.archer099/multi-redis-spring-boot-starter.svg)](https://search.maven.org/search?q=g:io.github.archer099%20a:multi-redis-spring-boot-starter)               | 多Redis源支持                   |
| [multi-redisson-spring-boot-starter](#multi-redisson-spring-boot-starter)         | [![Maven Central](https://img.shields.io/maven-central/v/io.github.archer099/multi-redisson-spring-boot-starter.svg)](https://search.maven.org/search?q=g:io.github.archer099%20a:multi-redisson-spring-boot-starter)         | 多Redisson数据源支持            |
| [mybatis-plus-spring3-boot-starter](#mybatis-plus-spring3-boot-starter)           | [![Maven Central](https://img.shields.io/maven-central/v/io.github.archer099/mybatis-plus-spring3-boot-starter.svg)](https://search.maven.org/search?q=g:io.github.archer099%20a:mybatis-plus-spring3-boot-starter)           | MyBatis-Plus与Spring Boot 3集成 |
| [sftp-spring-boot-starter](#sftp-spring-boot-starter)                             | [![Maven Central](https://img.shields.io/maven-central/v/io.github.archer099/sftp-spring-boot-starter.svg)](https://search.maven.org/search?q=g:io.github.archer099%20a:sftp-spring-boot-starter)                             | SFTP文件传输服务                |
| [dynamic-mq-spring-boot-starter](#dynamic-mq-spring-boot-starter)                 | [![Maven Central](https://img.shields.io/maven-central/v/io.github.archer099/dynamic-mq-spring-boot-starter.svg)](https://search.maven.org/search?q=g:io.github.archer099%20a:dynamic-mq-spring-boot-starter)                 | 动态消息队列管理                |
| [mqtt-spring-boot-starter](#mqtt-spring-boot-starter)                             | [![Maven Central](https://img.shields.io/maven-central/v/io.github.archer099/mqtt-spring-boot-starter.svg)](https://search.maven.org/search?q=g:io.github.archer099%20a:mqtt-spring-boot-starter)                             | MQTT消息协议支持                |
| [rate-limiter-spring-boot-starter](#rate-limiter-spring-boot-starter)             | [![Maven Central](https://img.shields.io/maven-central/v/io.github.archer099/rate-limiter-spring-boot-starter.svg)](https://search.maven.org/search?q=g:io.github.archer099%20a:rate-limiter-spring-boot-starter)             | 分布式限流组件                  |
| [lock-spring-boot-starter](#lock-spring-boot-starter)                             | [![Maven Central](https://img.shields.io/maven-central/v/io.github.archer099/lock-spring-boot-starter.svg)](https://search.maven.org/search?q=g:io.github.archer099%20a:lock-spring-boot-starter)                             | 分布式锁实现                    |
| [idempotent-spring-boot-starter](#idempotent-spring-boot-starter)                 | [![Maven Central](https://img.shields.io/maven-central/v/io.github.archer099/idempotent-spring-boot-starter.svg)](https://search.maven.org/search?q=g:io.github.archer099%20a:idempotent-spring-boot-starter)                 | 接口幂等性保障                  |
| [local-message-spring-boot-starter](#local-message-spring-boot-starter)           | [![Maven Central](https://img.shields.io/maven-central/v/io.github.archer099/local-message-spring-boot-starter.svg)](https://search.maven.org/search?q=g:io.github.archer099%20a:local-message-spring-boot-starter)           | 本地消息表                      |
| [ip2region-spring-boot-starter](#ip2region-spring-boot-starter)                   | [![Maven Central](https://img.shields.io/maven-central/v/io.github.archer099/ip2region-spring-boot-starter.svg)](https://search.maven.org/search?q=g:io.github.archer099%20a:ip2region-spring-boot-starter)                   | IP地址归属地查询                |
| [docs-spring-boot-starter](#docs-spring-boot-starter)                             | [![Maven Central](https://img.shields.io/maven-central/v/io.github.archer099/docs-spring-boot-starter.svg)](https://search.maven.org/search?q=g:io.github.archer099%20a:docs-spring-boot-starter)                             | API文档自动生成                 |
| [excel-spring-boot-starter](#excel-spring-boot-starter)                           | [![Maven Central](https://img.shields.io/maven-central/v/io.github.archer099/excel-spring-boot-starter.svg)](https://search.maven.org/search?q=g:io.github.archer099%20a:excel-spring-boot-starter)                           | Excel导入导出处理               |
| [crypto-spring-boot-starter](#crypto-spring-boot-starter)                         | [![Maven Central](https://img.shields.io/maven-central/v/io.github.archer099/crypto-spring-boot-starter.svg)](https://search.maven.org/search?q=g:io.github.archer099%20a:crypto-spring-boot-starter)                         | 加密解密工具集成                |
| [disruptor-spring-boot-starter](#disruptor-spring-boot-starter)                   | [![Maven Central](https://img.shields.io/maven-central/v/io.github.archer099/disruptor-spring-boot-starter.svg)](https://search.maven.org/search?q=g:io.github.archer099%20a:disruptor-spring-boot-starter)                   | 高性能队列Disruptor集成         |
| [dynamic-threadpool-spring-boot-starter](#dynamic-threadpool-spring-boot-starter) | [![Maven Central](https://img.shields.io/maven-central/v/io.github.archer099/dynamic-threadpool-spring-boot-starter.svg)](https://search.maven.org/search?q=g:io.github.archer099%20a:dynamic-threadpool-spring-boot-starter) | 动态线程池管理                  |
| [dict-spring-boot-starter](#dict-spring-boot-starter)                             | [![Maven Central](https://img.shields.io/maven-central/v/io.github.archer099/dict-spring-boot-starter.svg)](https://search.maven.org/search?q=g:io.github.archer099%20a:dict-spring-boot-starter)                             | 数据字典管理                    |
| [design-pattern-spring-boot-starter](#design-pattern-spring-boot-starter)         | [![Maven Central](https://img.shields.io/maven-central/v/io.github.archer099/design-pattern-spring-boot-starter.svg)](https://search.maven.org/search?q=g:io.github.archer099%20a:design-pattern-spring-boot-starter)         | 设计模式工具集                  |
| [local-cache-spring-boot-starter](#local-cache-spring-boot-starter)               | [![Maven Central](https://img.shields.io/maven-central/v/io.github.archer099/local-cache-spring-boot-starter.svg)](https://search.maven.org/search?q=g:io.github.archer099%20a:local-cache-spring-boot-starter)               | 本地缓存实现                    |
| [ffmpeg-spring-boot-starter](#ffmpeg-spring-boot-starter)                         | [![Maven Central](https://img.shields.io/maven-central/v/io.github.archer099/ffmpeg-spring-boot-starter.svg)](https://search.maven.org/search?q=g:io.github.archer099%20a:ffmpeg-spring-boot-starter)                         | FFmpeg视频处理                  |
| [sensitive-word-spring-boot-starter](#sensitive-word-spring-boot-starter)         | [![Maven Central](https://img.shields.io/maven-central/v/io.github.archer099/sensitive-word-spring-boot-starter.svg)](https://search.maven.org/search?q=g:io.github.archer099%20a:sensitive-word-spring-boot-starter)         | 敏感词过滤                      |
| [signature-spring-boot-starter](#signature-spring-boot-starter)                   | [![Maven Central](https://img.shields.io/maven-central/v/io.github.archer099/signature-spring-boot-starter.svg)](https://search.maven.org/search?q=g:io.github.archer099%20a:signature-spring-boot-starter)                   | API签名验证                     |
| [resilience4j-spring-boot-starter](#resilience4j-spring-boot-starter)             | [![Maven Central](https://img.shields.io/maven-central/v/io.github.archer099/resilience4j-spring-boot-starter.svg)](https://search.maven.org/search?q=g:io.github.archer099%20a:resilience4j-spring-boot-starter)             | Resilience4j容错组件            |

## 详细介绍

### 基础工具

#### common-tool-spring-boot-starter

通用工具集合组件，提供一系列通用工具类和功能，简化日常开发。

##### 主要功能

- **字符串工具**：丰富的字符串处理方法，包括格式化、转换和验证
- **日期时间工具**：简化日期时间操作，支持多种格式转换和计算
- **文件操作工具**：简化文件读写、复制、移动等操作
- **加密解密工具**：内置常用加密算法，如MD5、SHA、AES、RSA等
- **验证工具**：提供多种数据验证方法，如邮箱、手机号、身份证等
- **集合工具**：增强对List、Map等集合类型的操作
- **数字处理工具**：数值格式化、随机数生成、精度控制等功能

##### 使用示例

```java
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

---

#### i18n-spring-boot-starter

国际化支持组件，提供灵活的多语言支持，支持基于内存和资源文件的消息管理。

##### 主要功能

- **多语言支持**：灵活的多语言消息管理
- **内存消息管理**：支持基于内存的消息存储，方便动态添加
- **资源文件支持**：支持基于资源文件的消息管理
- **自定义消息源**：支持自定义消息源扩展
- **语言解析器**：内置多种语言解析策略
- **Web集成**：与Spring MVC无缝集成

##### 使用示例

```java
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
```

[查看详细文档](./i18n-spring-boot-starter/README.md)

---

#### design-pattern-spring-boot-starter

设计模式工具集组件，提供常用设计模式的快速实现。

##### 主要功能

- **策略模式**：灵活的策略模式实现，支持基于注解的策略注册
- **责任链模式**：链式处理器实现，支持动态编排
- **模板方法模式**：提供模板方法抽象类
- **工厂模式**：统一的工厂模式实现
- **观察者模式**：事件驱动的观察者模式实现

##### 使用示例

```java
// 策略模式使用
@Strategy(type = "PAY", value = "ALIPAY")
public class AlipayStrategy implements PayStrategy {
    @Override
    public void pay(Order order) {
        // 支付宝支付逻辑
    }
}

// 获取策略并执行
@Service
public class PayService {
    @Autowired
    private StrategyFactory<PayStrategy> strategyFactory;
    
    public void pay(String payType, Order order) {
        PayStrategy strategy = strategyFactory.getStrategy("PAY", payType);
        strategy.pay(order);
    }
}
```

[查看详细文档](./design-pattern-spring-boot-starter/README.md)

---

### 缓存与数据源

#### multi-redis-spring-boot-starter

多Redis数据源支持组件，用于在单个应用中连接和管理多个Redis实例。

##### 主要功能

- **多数据源配置**：支持配置多个Redis连接并自动注入
- **动态切换**：运行时动态切换Redis实例
- **连接池管理**：优化的连接池配置与监控
- **自动路由**：基于注解或方法名称的自动数据源路由
- **操作模板复用**：在不同数据源间复用RedisTemplate实例
- **集群支持**：兼容Redis单机、集群和哨兵模式

##### 使用示例

```java
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

---

#### multi-redisson-spring-boot-starter

Redisson多集群自动配置组件，支持一个Spring Boot服务连接多套Redis，支持单机、哨兵、主从、集群四种模式。

##### 主要功能

- **多实例支持**：支持配置任意数量的Redis实例
- **多模式支持**：支持单机、哨兵、主从、集群四种模式
- **双写支持**：支持双机房双写，备份集群异步写入
- **双写熔断**：支持熔断降级，备份集群故障时自动熔断
- **健康检查**：集成Spring Boot Actuator HealthIndicator
- **批量操作**：支持Redis Pipeline批量操作
- **动态配置**：支持Nacos等配置中心动态刷新配置
- **统一操作模板**：`MultiRedissonTemplate`封装双写逻辑

##### 使用示例

```java
@Service
public class RedisService {
    
    @Autowired
    private MultiRedissonTemplate redissonTemplate;
    
    public void demo() {
        // 设置值（自动双写）
        redissonTemplate.set("user:1", "张三");
        redissonTemplate.set("user:2", "李四", Duration.ofMinutes(30));
        
        // 获取值（只从主集群读取）
        String name = redissonTemplate.get("user:1");
        
        // Hash操作
        redissonTemplate.hset("user:info", "name", "张三");
        Map<String, Object> userInfo = redissonTemplate.hgetAll("user:info");
        
        // 分布式锁（只在主集群）
        RLock lock = redissonTemplate.getLock("lock:order:123");
        try {
            if (lock.tryLock(5, 30, TimeUnit.SECONDS)) {
                // 业务逻辑
            }
        } finally {
            lock.unlock();
        }
    }
}
```

[查看详细文档](./multi-redisson-spring-boot-starter/README.md)

---

#### local-cache-spring-boot-starter

本地缓存实现组件，提供高效的进程内缓存方案。

##### 主要功能

- **多种缓存实现**：支持Caffeine、Guava Cache等
- **注解支持**：通过注解实现缓存操作
- **过期策略**：支持多种缓存过期策略
- **容量控制**：支持缓存容量限制
- **监控统计**：缓存命中率和使用统计

##### 使用示例

```java
@Service
public class UserService {
    
    @LocalCache(key = "user:#id", expire = 300)
    public User getUser(Long id) {
        return userRepository.findById(id);
    }
    
    @LocalCacheEvict(key = "user:#id")
    public void updateUser(User user) {
        userRepository.save(user);
    }
}
```

[查看详细文档](./local-cache-spring-boot-starter/README.md)

---

#### mybatis-plus-spring3-boot-starter

MyBatis-Plus与Spring Boot 3集成组件，提供增强的ORM和CRUD操作支持。

##### 主要功能

- **无侵入增强**：在MyBatis基础上增强，不做任何侵入修改
- **Spring Boot 3适配**：完全适配Spring Boot 3和Jakarta EE规范
- **CRUD操作**：内置通用Mapper和Service，实现单表大部分CRUD操作
- **条件构造器**：强大的条件构造器，满足各类查询需求
- **分页插件**：内置分页插件，自动优化分页SQL
- **逻辑删除**：支持逻辑删除，保护数据安全
- **自动填充**：字段自动填充，节省重复代码

##### 使用示例

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

---

### 消息通信

#### dynamic-mq-spring-boot-starter

动态消息队列管理组件，支持多种消息队列平台的统一接入和动态配置。

##### 主要功能

- **多平台支持**：支持Kafka、RabbitMQ、RocketMQ、Redisson等多种消息队列
- **统一API**：提供统一的生产和消费接口
- **动态配置**：支持运行时动态修改队列配置
- **自动重连**：连接异常自动重连机制
- **消息追踪**：支持消息生产和消费全链路追踪
- **批量处理**：高效的批量消息处理
- **错误处理**：完善的异常处理和重试机制

##### 使用示例

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

---

#### mqtt-spring-boot-starter

基于Eclipse Paho MQTT客户端实现的Spring Boot Starter，支持EMQX等MQTT Broker。

##### 主要功能

- **开箱即用**：自动配置MQTT客户端，无需手动管理连接
- **注解驱动**：使用`@MqttMessageListener`注解轻松接收消息
- **模板发送**：提供`MqttTemplate`模板类，支持同步和异步发送
- **自动重连**：支持自动重连和断线重连后自动重新订阅
- **通配符订阅**：支持MQTT通配符（`+`和`#`）订阅
- **QoS支持**：支持QoS 0、1、2三种消息质量等级
- **共享订阅**：支持EMQX共享订阅和延迟发布
- **SSL/TLS加密**：支持SSL/TLS加密连接和双向认证

##### 使用示例

```java
@Service
@RequiredArgsConstructor
public class MessageService {
    private final MqttTemplate mqttTemplate;

    public void sendMessage() throws MqttException {
        // 发送简单消息
        mqttTemplate.send("testtopic/1", "Hello MQTT!");
        
        // 指定QoS
        mqttTemplate.send("testtopic/2", "Hello with QoS 2", 2);
        
        // 异步发送
        mqttTemplate.sendAsync("testtopic/4", "Async message")
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("发送失败", ex);
                    }
                });
    }
}

// 接收消息
@Component
public class MessageListener {
    @MqttMessageListener(topics = "testtopic/#", qos = 1)
    public void handleMessage(String topic, String payload) {
        log.info("收到消息 - 主题: {}, 内容: {}", topic, payload);
    }
}
```

[查看详细文档](./mqtt-spring-boot-starter/README.md)

---

#### robot-message-spring-boot-starter

多平台机器人消息通知组件，支持钉钉、企业微信、飞书等机器人消息通知。

##### 主要功能

- **多平台支持**：集成钉钉、企业微信、飞书等多种机器人平台
- **统一API**：提供统一的消息发送接口，简化多平台对接
- **丰富消息类型**：支持文本、Markdown、链接、图片等多种消息格式
- **可扩展架构**：基于SPI机制，方便扩展新的消息平台
- **配置热更新**：支持配置的动态更新，无需重启应用
- **脚本支持**：支持Groovy脚本动态生成消息内容

##### 使用示例

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

---

#### push-spring-boot-starter

移动端推送服务组件，支持多种主流推送平台。

##### 主要功能

- **多平台支持**：支持APNs（苹果）、FCM（谷歌）、极光推送、友盟推送
- **统一API**：提供统一的推送接口，简化多平台对接
- **丰富消息类型**：支持通知消息、透传消息、应用内消息等
- **批量推送**：支持批量推送和分组推送
- **定时推送**：支持定时推送和模板推送
- **回调支持**：支持推送结果回调

##### 使用示例

```java
@Autowired
private MessageNotificationFactory notificationFactory;

// 发送推送通知
public void sendPush(String deviceToken, String title, String content) {
    MessageNotification notification = notificationFactory.getNotification(NotificationType.JIGUANG);
    SimpleNotificationRequest request = new SimpleNotificationRequest();
    request.setTitle(title);
    request.setContent(content);
    request.setDeviceToken(deviceToken);
    notification.send(request);
}
```

[查看详细文档](./push-spring-boot-starter/README.md)

---

#### netty-spring-boot-starter

Netty服务器与WebSocket支持组件，简化基于Netty的高性能WebSocket服务开发。

##### 主要功能

- **WebSocket服务器**：快速搭建基于Netty的WebSocket服务
- **注解驱动开发**：通过注解定义WebSocket端点和处理方法
- **会话管理**：内置WebSocket会话管理，支持会话属性存储
- **身份认证**：提供WebSocket连接认证机制
- **集群支持**：基于Redis的集群会话管理和消息广播
- **心跳检测**：自动心跳检测与连接维护
- **二进制消息**：支持文本和二进制消息处理

##### 使用示例

```java
@WebSocketEndpoint(path = "/ws/chat")
public class ChatEndpoint {

    @OnOpen
    public void onOpen(WebSocketSession session) {
        System.out.println("连接建立: " + session.getId());
    }

    @OnMessage
    public void onMessage(WebSocketSession session, String message) {
        System.out.println("收到消息: " + message);
        session.sendText("服务器已收到消息: " + message);
    }

    @OnClose
    public void onClose(WebSocketSession session) {
        System.out.println("连接关闭: " + session.getId());
    }
}
```

[查看详细文档](./netty-spring-boot-starter/README.md)

---

### 分布式组件

#### resilience4j-spring-boot-starter

Resilience4j 容错组件，提供熔断器、限流器、重试、舱壁、时间限制器等完整功能。

##### 主要功能

- **熔断器（Circuit Breaker）**：自动检测服务故障并触发熔断，防止故障扩散
- **限流器（Rate Limiter）**：控制请求速率，防止系统过载
- **重试（Retry）**：失败后自动重试，提高成功率
- **舱壁（Bulkhead）**：限制并发调用数，实现资源隔离
- **时间限制器（Time Limiter）**：控制方法执行超时
- **组合使用**：支持多个功能组合使用
- **多种降级策略**：支持异常、方法、默认值、null 四种降级策略
- **灵活配置**：支持注解和配置文件两种配置方式

##### 使用示例

```java
@Service
public class OrderService {
    
    // 熔断器
    @CircuitBreaker(
        name = "orderService",
        failureRateThreshold = 50.0f,
        fallbackStrategy = FallbackStrategy.METHOD,
        fallbackMethod = "createOrderFallback"
    )
    public Order createOrder(OrderRequest request) {
        return remoteOrderService.create(request);
    }
    
    public Order createOrderFallback(OrderRequest request, Throwable throwable) {
        log.error("创建订单失败，使用降级逻辑", throwable);
        return Order.failed();
    }
    
    // 限流器
    @RateLimiter(
        name = "apiService",
        limitForPeriod = 10,
        fallbackStrategy = FallbackStrategy.METHOD,
        fallbackMethod = "apiLimitFallback"
    )
    public ApiResponse callApi(ApiRequest request) {
        return apiClient.call(request);
    }
    
    public ApiResponse apiLimitFallback(ApiRequest request, Throwable throwable) {
        return ApiResponse.error("请求过于频繁，请稍后重试");
    }
    
    // 重试
    @Retry(
        name = "paymentService",
        maxAttempts = 3,
        waitDuration = 1000
    )
    public PaymentResult pay(PaymentRequest request) {
        return paymentGateway.pay(request);
    }
    
    // 舱壁
    @Bulkhead(
        name = "reportService",
        maxConcurrentCalls = 5,
        type = Bulkhead.Type.SEMAPHORE
    )
    public Report generateReport(ReportRequest request) {
        return reportGenerator.generate(request);
    }
    
    // 时间限制器
    @TimeLimiter(
        name = "searchService",
        timeoutDuration = 3000
    )
    public SearchResult search(SearchQuery query) {
        return searchEngine.search(query);
    }
    
    // 组合使用
    @Retry(name = "combined", maxAttempts = 3)
    @CircuitBreaker(name = "combined")
    @RateLimiter(name = "combined", limitForPeriod = 10)
    public String callRemoteService() {
        return remoteService.call();
    }
}
```

##### 配置示例

```yaml
resilience4j:
  circuit-breaker:
    instances:
      orderService:
        failure-rate-threshold: 50.0
        slow-call-rate-threshold: 80.0
        minimum-number-of-calls: 10
  
  rate-limiter:
    instances:
      apiService:
        limit-for-period: 10
        limit-refresh-period: 1000000000
  
  retry:
    instances:
      paymentService:
        max-attempts: 3
        wait-duration: 1000
```

[查看详细文档](./resilience4j-spring-boot-starter/README.md)

---

#### lock-spring-boot-starter

分布式锁实现组件，提供基于Redis或Zookeeper的分布式锁。

##### 主要功能

- **多种实现**：支持Redis、Zookeeper等多种分布式锁实现
- **注解支持**：通过注解轻松添加分布式锁
- **锁超时与续期**：自动管理锁超时和续期
- **可重入锁**：支持可重入锁机制
- **读写锁**：支持读写分离锁
- **尝试锁**：支持尝试获取锁与等待超时
- **锁释放保障**：确保锁在异常情况下也能正确释放

##### 使用示例

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

---

#### rate-limiter-spring-boot-starter

分布式限流组件，提供多种限流策略和易用的注解配置。

##### 主要功能

- **多种限流算法**：支持令牌桶、漏桶、窗口计数等限流算法
- **注解驱动**：通过简单注解实现方法级限流
- **分布式支持**：基于Redis的分布式限流实现
- **灵活配置**：支持全局和细粒度限流配置
- **降级处理**：提供限流触发后的降级处理机制
- **监控统计**：限流指标监控和统计
- **按条件限流**：支持按IP、用户ID等维度进行限流

##### 使用示例

```java
@Service
public class OrderService {
    
    @RateLimit(key = "createOrder", limit = 10, period = 1, unit = TimeUnit.MINUTES)
    public void createOrder(OrderDTO order) {
        // 创建订单逻辑
    }
    
    @RateLimit(key = "queryOrder", limit = 50, period = 1, unit = TimeUnit.SECONDS)
    public OrderDTO getOrder(String orderId) {
        return new OrderDTO();
    }
}
```

[查看详细文档](./rate-limiter-spring-boot-starter/README.md)

---

#### idempotent-spring-boot-starter

接口幂等性保障组件，防止重复请求和重复处理。

##### 主要功能

- **自动去重**：自动识别和拦截重复请求
- **多种策略**：支持基于Token、参数、ID等多种幂等策略
- **注解驱动**：通过注解简单实现幂等控制
- **过期机制**：可配置的幂等记录过期时间
- **分布式支持**：基于Redis的分布式幂等实现
- **适用场景广**：适用于API接口、消息处理等多种场景
- **自定义扩展**：支持自定义幂等键生成策略

##### 使用示例

```java
@RestController
@RequestMapping("/orders")
public class OrderController {
    
    @PostMapping("/create")
    @Idempotent(key = "create:order:#{#request.orderNo}", expireTime = 5, timeUnit = TimeUnit.MINUTES)
    public ResponseEntity<Order> createOrder(@RequestBody OrderRequest request) {
        return ResponseEntity.ok(orderService.create(request));
    }
    
    @PostMapping("/pay")
    @Idempotent(type = IdempotentType.TOKEN, tokenHeader = "Idempotent-Token")
    public ResponseEntity<PaymentResult> payOrder(@RequestBody PaymentRequest request) {
        return ResponseEntity.ok(paymentService.processPayment(request));
    }
}
```

[查看详细文档](./idempotent-spring-boot-starter/README.md)

---

#### local-message-spring-boot-starter

本地消息表Spring Boot Starter，用于实现分布式事务的最终一致性。

##### 主要功能

- **事务一致性**：消息与业务数据在同一事务中提交，保证强一致性
- **消息不丢失**：基于数据库持久化，避免消息丢失
- **自动重试**：支持指数退避重试策略，可配置最大重试次数
- **策略模式**：支持多种业务类型，每种类型可有独立的处理器
- **并发处理**：支持虚拟线程异步处理消息
- **乐观锁**：防止消息重复处理

##### 使用示例

```java
@Service
public class OrderService {
    
    @Autowired
    private LocalMessageService localMessageService;
    
    @Transactional(rollbackFor = Exception.class)
    public void createOrder(String orderId, String orderData) {
        // 1. 保存业务数据
        saveOrderToDatabase(orderId, orderData);
        
        // 2. 发送本地消息（同一事务）
        localMessageService.sendMessage(
            "ORDER",           // 业务类型
            orderId,           // 业务ID
            messageContent,    // 消息内容
            3,                 // 最大重试次数
            null               // 扩展数据
        );
    }
}

// 消息处理器
@Component
public class OrderMessageProcessor implements MessageProcessor {
    
    @Override
    public String getBusinessType() {
        return "ORDER";
    }
    
    @Override
    public ProcessResult process(LocalMessage message) {
        try {
            callExternalApi(message);
            return ProcessResult.success();
        } catch (Exception e) {
            return ProcessResult.failureWithRetry("调用失败: " + e.getMessage(), 60L);
        }
    }
}
```

[查看详细文档](./local-message-spring-boot-starter/README.md)

---

### 任务调度与线程池

#### xxl-job-spring-boot-starter

基于XXL-JOB的分布式任务调度组件，提供任务的自动注册功能。

##### 主要功能

- **执行器自动注册**：启动时自动向XXL-JOB调度中心注册当前应用为执行器
- **任务自动注册**：通过注解自动注册任务到XXL-JOB调度中心
- **简化配置**：提供简洁的配置方式，降低接入成本
- **完整参数支持**：支持XXL-JOB的所有调度参数配置
- **分片任务支持**：提供便捷的分片任务工具类

##### 使用示例

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

---

#### dynamic-threadpool-spring-boot-starter

动态线程池管理组件，支持运行时动态调整线程池参数。

##### 主要功能

- **动态调参**：支持运行时动态调整核心线程数、最大线程数、队列容量等
- **监控端点**：集成Spring Boot Actuator，提供线程池监控端点
- **告警通知**：支持钉钉、企业微信等告警通知
- **配置中心集成**：支持Nacos等配置中心动态刷新
- **第三方线程池适配**：支持Tomcat、HikariCP等第三方线程池监控
- **注解支持**：通过注解标记动态线程池

##### 使用示例

```java
@Configuration
public class ThreadPoolConfig {
    
    @Bean
    @DynamicThreadPool
    public ThreadPoolExecutor orderThreadPool() {
        return new ThreadPoolExecutor(
            5, 10, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000),
            new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }
}

// YAML配置
// dynamic-thread-pool:
//   enabled: true
//   pools:
//     orderThreadPool:
//       core-pool-size: 5
//       maximum-pool-size: 10
//       keep-alive-time: 60
//       queue-capacity: 1000
```

[查看详细文档](./dynamic-threadpool-spring-boot-starter/README.md)

---

#### disruptor-spring-boot-starter

高性能队列Disruptor集成组件，提供LMAX Disruptor的快速集成。

##### 主要功能

- **高性能队列**：基于Disruptor实现的高性能无锁队列
- **注解支持**：通过注解快速定义事件处理器
- **多种等待策略**：支持BlockingWait、BusySpinWait等多种等待策略
- **事件批处理**：支持事件批量处理
- **异常处理**：完善的异常处理机制

##### 使用示例

```java
@DisruptorEvent(bufferSize = 1024, waitStrategy = WaitStrategyType.BLOCKING_WAIT)
public class OrderEvent {
    private String orderId;
    private String orderData;
    // getter/setter
}

@DisruptorEventHandler(event = OrderEvent.class)
public class OrderEventHandler implements EventHandler<OrderEvent> {
    @Override
    public void onEvent(OrderEvent event, long sequence, boolean endOfBatch) {
        // 处理订单事件
    }
}
```

[查看详细文档](./disruptor-spring-boot-starter/README.md)

---

### 文件与存储

#### oss-spring-boot-starter

对象存储服务集成组件，提供统一的文件上传下载API，支持多种对象存储服务。

##### 主要功能

- **多供应商支持**：兼容AWS S3、阿里云OSS、腾讯云COS等多种对象存储服务
- **统一API**：提供统一的文件操作接口，屏蔽底层实现差异
- **简化操作**：封装常用的文件上传、下载、删除等操作
- **分片上传**：支持大文件分片上传和断点续传
- **上传进度监控**：提供上传进度回调功能

##### 使用示例

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

---

#### sftp-spring-boot-starter

SFTP文件传输服务组件，提供安全、高效的文件传输功能。

##### 主要功能

- **自动连接管理**：自动建立和释放SFTP连接
- **连接池支持**：内置连接池，提高传输效率
- **并发传输**：支持并发上传和下载
- **断点续传**：支持大文件分片上传与断点续传
- **路径配置**：灵活的路径配置和目录管理
- **安全传输**：基于SSH的安全传输机制

##### 使用示例

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

---

#### excel-spring-boot-starter

Excel导入导出处理组件，提供简洁的Excel操作API。

##### 主要功能

- **导入导出**：支持Excel文件的导入和导出
- **注解驱动**：通过注解定义Excel列映射
- **大数据支持**：支持大数据量导出，避免内存溢出
- **多Sheet支持**：支持多Sheet页处理
- **模板导出**：支持基于模板的Excel导出
- **数据校验**：导入时自动校验数据

##### 使用示例

```java
@Data
public class UserExcel {
    @ExcelProperty("用户名")
    private String username;
    
    @ExcelProperty("邮箱")
    private String email;
    
    @ExcelProperty("创建时间")
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}

@RestController
public class ExcelController {
    @Autowired
    private ExcelService excelService;
    
    @PostMapping("/import")
    public void importUsers(@RequestParam MultipartFile file) {
        List<UserExcel> users = excelService.read(file, UserExcel.class);
        // 处理导入数据
    }
    
    @GetMapping("/export")
    public void exportUsers(HttpServletResponse response) {
        List<UserExcel> users = userService.listAll();
        excelService.write(response, "用户列表", users, UserExcel.class);
    }
}
```

[查看详细文档](./excel-spring-boot-starter/README.md)

---

### 数据处理

#### desensitize-spring-boot-starter

数据脱敏处理组件，支持多种脱敏场景和自定义脱敏规则。

##### 主要功能

- **多场景支持**：支持手机号、邮箱、身份证、银行卡等多种类型数据脱敏
- **多框架集成**：无缝集成Jackson、FastJson等主流JSON框架
- **注解驱动**：通过简单注解实现字段级脱敏
- **灵活配置**：支持全局和定制化脱敏规则配置
- **条件脱敏**：支持基于条件的动态脱敏处理
- **可扩展性**：支持自定义脱敏处理器和规则

##### 使用示例

```java
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
}

// 控制器使用 - 返回时自动对标记的字段进行脱敏
@GetMapping("/{id}")
public ResponseEntity<UserDTO> getUser(@PathVariable Long id) {
    return ResponseEntity.ok(userService.getUser(id));
}
```

[查看详细文档](./desensitize-spring-boot-starter/README.md)

---

#### crypto-spring-boot-starter

加密解密工具集成组件，支持多种加密算法和场景。

##### 主要功能

- **多种算法**：支持AES、RSA、SM2、SM4等多种加密算法
- **注解驱动**：通过注解实现字段级加解密
- **请求响应加密**：支持HTTP请求体和响应体加解密
- **MyBatis集成**：支持数据库字段自动加解密
- **密钥管理**：灵活的密钥配置和管理

##### 使用示例

```java
// 请求参数加解密
@RestController
@EnableParamsCrypto
public class UserController {
    
    @PostMapping("/create")
    @ParamsCrypto(encryptResponse = true, decryptRequest = true)
    public UserResponse createUser(@RequestBody UserRequest request) {
        // request中的加密字段会自动解密
        // response会自动加密返回
        return userService.create(request);
    }
}

// 字段级加密
public class UserEntity {
    @CryptoField(type = CryptoType.AES)
    private String phone;
    
    @CryptoField(type = CryptoType.SM4)
    private String idCard;
}
```

[查看详细文档](./crypto-spring-boot-starter/README.md)

---

#### dict-spring-boot-starter

数据字典管理组件，提供字典数据的统一管理和翻译。

##### 主要功能

- **字典翻译**：自动将字典值翻译为字典文本
- **注解支持**：通过注解标记需要翻译的字段
- **缓存机制**：内置字典缓存，提高查询效率
- **动态刷新**：支持字典数据动态刷新
- **多数据源**：支持从数据库、配置文件等多种数据源加载字典

##### 使用示例

```java
public class OrderDTO {
    private Long id;
    
    @DictField(type = "ORDER_STATUS")
    private String status;
    
    private String statusText;  // 自动填充翻译后的文本
}
```

[查看详细文档](./dict-spring-boot-starter/README.md)

---

#### ip2region-spring-boot-starter

IP地址归属地查询组件，快速准确地查询IP地址的地理位置信息。

##### 主要功能

- **快速查询**：毫秒级IP地址归属地查询
- **内存优化**：多种查询算法，适应不同内存需求
- **准确度高**：基于最新IP地址库，提供精确查询结果
- **缓存机制**：内置缓存机制，提高查询效率
- **离线查询**：完全离线查询，不依赖外部服务

##### 使用示例

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

---

#### sensitive-word-spring-boot-starter

敏感词过滤组件，基于DFA（确定性有限自动机）算法实现高性能敏感词检测和过滤。

##### 主要功能

- **高性能**：基于DFA算法，时间复杂度O(n)，适合大量文本检测
- **多种匹配模式**：支持最小匹配和最大匹配两种模式
- **多种处理策略**：支持替换、抛异常、高亮、仅检测四种处理方式
- **注解驱动**：通过注解实现方法参数和对象字段的敏感词检测
- **白名单支持**：支持配置白名单词汇，避免误杀
- **词库管理**：支持从配置、文件、classpath多种方式加载词库
- **动态管理**：支持运行时动态添加、删除敏感词

##### 使用示例

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
    }
}

// 使用注解
@PostMapping("/submit")
@SensitiveWordCheck(fields = {"title", "content"}, handleType = HandleType.REPLACE)
public Result<Article> submitArticle(@RequestBody Article article) {
    return Result.success(article);
}
```

[查看详细文档](./sensitive-word-spring-boot-starter/README.md)

---

#### signature-spring-boot-starter

API签名验证组件，支持防篡改、防重放攻击。

##### 主要功能

- **防篡改**：基于HMAC/SHA算法对请求参数签名，防止数据被篡改
- **防重放**：基于Nonce + Timestamp机制，防止请求被重放攻击
- **多种算法**：支持MD5、SHA1、SHA256、SHA512、HMAC-SHA256、HMAC-SHA512
- **注解驱动**：支持`@SignatureVerify`和`@IgnoreSignature`注解
- **存储接口**：NonceStore和AppSecretStore提供接口，由用户实现具体存储
- **内置实现**：提供基于内存的默认实现（适合单机部署）

##### 使用示例

```java
// 类级别启用签名验证
@RestController
@RequestMapping("/api")
@SignatureVerify
public class ApiController {
    
    @GetMapping("/data")
    public Result getData() {
        // 需要签名验证
    }
    
    @PostMapping("/submit")
    @SignatureVerify(verifyBody = true)
    public Result submit(@RequestBody Data data) {
        // 需要签名验证，包含请求体
    }
    
    @GetMapping("/public")
    @IgnoreSignature
    public Result publicApi() {
        // 忽略签名验证
    }
}

// 自定义 NonceStore（Redis实现）
@Component
public class RedisNonceStore implements NonceStore {
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    @Override
    public boolean storeIfAbsent(String nonce, Duration ttl) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue()
                .setIfAbsent("signature:nonce:" + nonce, "1", ttl));
    }
}
```

[查看详细文档](./signature-spring-boot-starter/README.md)

---

### 脚本与扩展

#### script-spring-boot-starter

多语言脚本执行支持，提供在Java应用中动态执行脚本的能力。

##### 主要功能

- **多语言支持**：支持Groovy、JavaScript、Lua、Python等多种脚本语言
- **统一执行接口**：提供统一的脚本执行API
- **脚本缓存**：支持脚本编译结果缓存，提高执行效率
- **执行超时控制**：防止脚本执行时间过长
- **安全沙箱**：限制脚本执行环境，防止恶意代码
- **数据交互**：支持Java与脚本之间的数据交互

##### 使用示例

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

---

#### ffmpeg-spring-boot-starter

FFmpeg视频处理组件，提供FFmpeg通用能力封装。

##### 主要功能

- **视频转码**：支持多种视频格式转码
- **音频处理**：支持音频提取、转换等操作
- **HLS切片**：支持将音视频转换为HLS格式
- **媒体信息**：获取媒体文件的详细信息
- **命令执行**：封装FFmpeg命令执行

##### 使用示例

```java
@Autowired
private FfmpegService ffmpegService;

// 获取媒体信息
FfprobeResult info = ffmpegService.probe("/path/to/video.mp4");

// 音频转HLS
AudioHlsRequest request = new AudioHlsRequest();
request.setInputPath("/path/to/audio.mp3");
request.setOutputPath("/path/to/output");
request.setSegmentDuration(10);
AudioHlsResult result = ffmpegService.audioToHls(request);
```

[查看详细文档](./ffmpeg-spring-boot-starter/README.md)

---

### 接口文档

#### docs-spring-boot-starter

API文档自动生成组件，支持多种文档格式和样式定制。

##### 主要功能

- **接口扫描**：自动扫描并记录API接口
- **多格式支持**：支持Swagger、OpenAPI、Markdown等多种文档格式
- **丰富注解**：丰富的文档注解，详细描述API信息
- **分组管理**：API分组管理和权限控制
- **在线调试**：支持在线接口调试功能
- **文档导出**：支持导出为多种格式的离线文档

##### 使用示例

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

---

## 技术栈

- Java 21
- Spring Boot 3.x
- Hutool 工具集
- Jakarta Validation API
- Jackson
- 各种特定领域组件集成

## 如何选择合适的组件

| 需求场景                             | 推荐组件                                                             |
| ------------------------------------ | -------------------------------------------------------------------- |
| 基础工具支持（如字符串、日期处理等） | common-tool-spring-boot-starter                                      |
| 国际化多语言支持                     | i18n-spring-boot-starter                                             |
| 分布式任务调度                       | xxl-job-spring-boot-starter                                          |
| 机器人消息通知（钉钉、企业微信等）   | robot-message-spring-boot-starter                                    |
| 移动端推送（APNs、FCM、极光等）      | push-spring-boot-starter                                             |
| 对象存储服务                         | oss-spring-boot-starter                                              |
| 脚本执行能力                         | script-spring-boot-starter                                           |
| WebSocket/实时通信                   | netty-spring-boot-starter                                            |
| MQTT物联网通信                       | mqtt-spring-boot-starter                                             |
| 数据脱敏处理                         | desensitize-spring-boot-starter                                      |
| 多Redis数据源                        | multi-redis-spring-boot-starter / multi-redisson-spring-boot-starter |
| 分布式锁                             | lock-spring-boot-starter                                             |
| 接口限流                             | rate-limiter-spring-boot-starter                                     |
| 接口幂等                             | idempotent-spring-boot-starter                                       |
| 分布式事务最终一致性                 | local-message-spring-boot-starter                                    |
| 动态线程池管理                       | dynamic-threadpool-spring-boot-starter                               |
| 视频音频处理                         | ffmpeg-spring-boot-starter                                           |
| 敏感词过滤/内容审核                  | sensitive-word-spring-boot-starter                                   |
| API签名验证/防篡改                   | signature-spring-boot-starter                                        |
| 容错保护（熔断/限流/重试）           | resilience4j-spring-boot-starter                                     |

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
