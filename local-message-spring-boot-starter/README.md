# Local Message Spring Boot Starter

本地消息表 Spring Boot Starter，用于实现分布式事务的最终一致性。通过在业务事务中写入本地消息表，然后异步处理消息来调用外部接口，保证消息不丢失且支持重试机制。

## 特性

- ✅ **事务一致性**：消息与业务数据在同一事务中提交，保证强一致性
- ✅ **消息不丢失**：基于数据库持久化，避免消息丢失
- ✅ **自动重试**：支持指数退避重试策略，可配置最大重试次数
- ✅ **策略模式**：支持多种业务类型，每种类型可有独立的处理器
- ✅ **并发处理**：支持虚拟线程异步处理消息，提供更好的并发性能
- ✅ **乐观锁**：防止消息重复处理
- ✅ **灵活配置**：支持自定义扫描间隔、线程池等参数

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>io.github.kk01001</groupId>
    <artifactId>local-message-spring-boot-starter</artifactId>
    <version>2.4.7</version>
</dependency>
```

### 2. 创建数据库表

执行 `src/main/resources/sql/local_message.sql` 中的SQL脚本创建本地消息表。

### 3. 实现数据访问层

实现 `LocalMessageDao` 接口：

```java
@Repository
public class LocalMessageDaoImpl implements LocalMessageDao {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Override
    public int insert(LocalMessage message) {
        // 实现插入逻辑
    }
    
    @Override
    public LocalMessage selectById(Long id) {
        // 实现查询逻辑
    }
    
    // ... 其他方法实现
}
```

### 4. 实现消息处理器

为每种业务类型实现 `MessageProcessor` 接口：

```java
@Component
public class OrderMessageProcessor implements MessageProcessor {
    
    @Override
    public String getBusinessType() {
        return "ORDER";
    }
    
    @Override
    public ProcessResult process(LocalMessage message) {
        try {
            // 调用外部接口
            callExternalApi(message);
            return ProcessResult.success();
        } catch (Exception e) {
            // 处理失败，需要重试
            return ProcessResult.failureWithRetry("调用失败: " + e.getMessage(), 60L);
        }
    }
}
```

### 5. 在业务代码中发送消息

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
            messageContent,    // 消息内容（JSON）
            3,                 // 最大重试次数
            null               // 扩展数据
        );
    }
}
```

## 配置说明

在 `application.yml` 中配置：

```yaml
local-message:
  enabled: true  # 是否启用，默认true
  scheduler:
    pending-scan-interval: 30000    # 待处理消息扫描间隔（毫秒），默认30秒
    retry-scan-interval: 60000      # 重试消息扫描间隔（毫秒），默认60秒
    batch-size: 100                 # 每次扫描的批次大小，默认100
  thread-pool:
    core-pool-size: 5              # 核心线程数，默认5
    maximum-pool-size: 20          # 最大线程数，默认20
    keep-alive-time: 60            # 线程空闲时间（秒），默认60
    queue-capacity: 1000           # 队列容量，默认1000
    thread-name-prefix: "local-message-"  # 线程名前缀
```

## 核心组件

### LocalMessage 实体

本地消息实体，包含以下主要字段：

- `businessType`: 业务类型，用于路由到对应的处理器
- `businessId`: 业务ID，关联的业务数据主键
- `messageContent`: 消息内容，建议使用JSON格式
- `status`: 消息状态（待处理、处理中、成功、失败等）
- `retryCount`: 当前重试次数
- `maxRetryCount`: 最大重试次数
- `nextRetryTime`: 下次重试时间
- `version`: 版本号，用于乐观锁

### MessageProcessor 接口

消息处理器接口，使用策略模式：

```java
public interface MessageProcessor {
    String getBusinessType();           // 返回支持的业务类型
    ProcessResult process(LocalMessage message);  // 处理消息
}
```

### ProcessResult 处理结果

- `ProcessResult.success()`: 处理成功
- `ProcessResult.failure(errorMessage)`: 处理失败，不重试
- `ProcessResult.failureWithRetry(errorMessage)`: 处理失败，需要重试
- `ProcessResult.failureWithRetry(errorMessage, delaySeconds)`: 处理失败，指定重试延迟

## 重试策略

默认使用指数退避策略：
- 第1次重试：1分钟后
- 第2次重试：2分钟后
- 第3次重试：4分钟后
- 第4次重试：8分钟后
- ...
- 最大延迟：30分钟

可以在 `ProcessResult.failureWithRetry()` 中指定自定义延迟时间。

## 消息状态流转

```
待处理(PENDING) 
    ↓
处理中(PROCESSING) 
    ↓
成功(SUCCESS) / 失败(FAILED) / 达到最大重试次数(MAX_RETRY_REACHED)
```

## 最佳实践

1. **事务管理**：确保业务方法添加 `@Transactional` 注解
2. **幂等性**：外部接口调用应该具备幂等性
3. **监控告警**：监控失败和达到最大重试次数的消息
4. **消息清理**：定期清理已成功处理的历史消息
5. **错误处理**：在处理器中妥善处理异常，返回合适的 `ProcessResult`

## 示例代码

完整的使用示例请参考 `src/test/java/io/github/kk01001/localmessage/example/` 目录下的代码。

## 注意事项

1. 需要用户自行实现 `LocalMessageDao` 接口
2. 需要用户为每种业务类型实现对应的 `MessageProcessor`
3. 建议在生产环境中配置数据库连接池和事务管理器
4. 消息处理器中的外部调用应该具备幂等性
5. 建议定期清理已处理成功的历史消息数据

## 许可证

Apache License 2.0
