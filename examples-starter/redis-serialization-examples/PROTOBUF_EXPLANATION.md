# Protobuf 序列化说明

## 为什么使用 Protostuff 而不是 Google Protobuf？

### Google Protocol Buffers
- **优点**：跨语言、高效、标准化
- **缺点**：需要定义 `.proto` 文件、需要编译生成代码、使用复杂

### Protostuff
- **优点**：无需定义 `.proto` 文件、自动序列化 Java 对象、使用简单
- **缺点**：仅支持 Java（但我们的场景是 Java to Java）

## Redisson 的 ProtobufCodec

Redisson 内置的 `ProtobufCodec` 实际上支持两种方式：

### 1. 原生 Google Protobuf
如果你的类实现了 `MessageLite` 接口（Google Protobuf 生成的类），会使用原生序列化：

```java
// 需要定义 .proto 文件并生成代码
public class UserProto extends GeneratedMessageV3 implements MessageLite {
    // Google Protobuf 生成的代码
}
```

### 2. Protostuff（推荐）
对于普通的 Java POJO，会自动使用 Protostuff 序列化：

```java
// 无需任何特殊处理，普通 POJO 即可
@Data
public class User {
    private String id;
    private String name;
    private String email;
    private Integer age;
}
```

## 我们的实现

在本项目中，我们选择使用 **Protostuff** 方式，因为：

1. **简单**：无需定义 `.proto` 文件
2. **自动**：自动处理 Java 对象序列化
3. **高效**：性能接近原生 Protobuf
4. **灵活**：支持任意 Java 对象

## 使用方法

### 1. 添加依赖

```xml
<dependency>
    <groupId>io.protostuff</groupId>
    <artifactId>protostuff-core</artifactId>
</dependency>
<dependency>
    <groupId>io.protostuff</groupId>
    <artifactId>protostuff-runtime</artifactId>
</dependency>
```

### 2. 配置序列化方式

```yaml
spring:
  data:
    redis:
      codec-type: PROTOBUF
```

### 3. 使用（无需任何特殊代码）

```java
@Service
public class UserService {
    
    @Autowired
    private RedissonClient redissonClient;
    
    public void saveUser(User user) {
        // Protostuff 会自动序列化
        RBucket<User> bucket = redissonClient.getBucket("user:" + user.getId());
        bucket.set(user);
    }
    
    public User getUser(String userId) {
        // Protostuff 会自动反序列化
        RBucket<User> bucket = redissonClient.getBucket("user:" + userId);
        return bucket.get();
    }
}
```

## Protostuff 工作原理

### 序列化过程

1. **运行时 Schema 生成**
   ```java
   Schema<User> schema = RuntimeSchema.getSchema(User.class);
   ```

2. **对象序列化**
   ```java
   LinkedBuffer buffer = LinkedBuffer.allocate();
   byte[] bytes = ProtostuffIOUtil.toByteArray(user, schema, buffer);
   ```

### 反序列化过程

1. **创建对象实例**
   ```java
   User user = schema.newMessage();
   ```

2. **填充数据**
   ```java
   ProtostuffIOUtil.mergeFrom(bytes, user, schema);
   ```

## 性能特点

### 内存占用
- **非常小**：二进制格式，接近原生 Protobuf
- **无元数据**：不包含字段名称
- **紧凑编码**：使用变长编码

### 序列化速度
- **快速**：无需反射（使用运行时生成的代码）
- **零拷贝**：直接写入 ByteBuf
- **批量处理**：支持批量序列化

### 反序列化速度
- **快速**：直接读取二进制数据
- **延迟加载**：支持部分字段读取
- **类型安全**：编译时类型检查

## 与其他序列化方式对比

| 特性 | Protostuff | Google Protobuf | Kryo | FST | JSON |
|------|-----------|----------------|------|-----|------|
| 需要定义文件 | ❌ | ✅ | ❌ | ❌ | ❌ |
| 内存占用 | 很小 | 最小 | 小 | 中等 | 大 |
| 序列化速度 | 快 | 快 | 很快 | 最快 | 中等 |
| 跨语言 | ❌ | ✅ | ❌ | ❌ | ✅ |
| 可读性 | ❌ | ❌ | ❌ | ❌ | ✅ |
| 使用难度 | 简单 | 复杂 | 简单 | 简单 | 简单 |

## 适用场景

### ✅ 适合使用 Protostuff

1. **纯 Java 应用**：不需要跨语言
2. **内存敏感**：需要最小化内存占用
3. **快速开发**：不想定义 `.proto` 文件
4. **动态对象**：对象结构经常变化

### ❌ 不适合使用 Protostuff

1. **跨语言通信**：需要与其他语言交互
2. **严格版本控制**：需要精确的版本兼容性
3. **调试需求**：需要人类可读的格式
4. **标准化要求**：需要使用标准协议

## 注意事项

### 1. 类结构变化

Protostuff 对类结构变化比较敏感：

```java
// 原始类
public class User {
    private String name;
    private int age;
}

// 添加字段 - ✅ 兼容
public class User {
    private String name;
    private int age;
    private String email;  // 新字段
}

// 删除字段 - ⚠️ 可能有问题
public class User {
    private String name;
    // age 字段被删除
}

// 修改字段类型 - ❌ 不兼容
public class User {
    private String name;
    private String age;  // int -> String
}
```

### 2. 性能优化

```java
// 使用对象池
private static final ThreadLocal<LinkedBuffer> bufferThreadLocal = 
    ThreadLocal.withInitial(() -> LinkedBuffer.allocate(512));

// 批量序列化
List<User> users = ...;
for (User user : users) {
    LinkedBuffer buffer = bufferThreadLocal.get();
    try {
        byte[] bytes = ProtostuffIOUtil.toByteArray(user, schema, buffer);
        // 处理 bytes
    } finally {
        buffer.clear();  // 重用 buffer
    }
}
```

### 3. 线程安全

- `Schema` 是线程安全的，可以缓存
- `LinkedBuffer` 不是线程安全的，使用 ThreadLocal

## 总结

Protostuff 是一个优秀的 Java 序列化框架，特别适合：
- 需要高性能序列化的 Java 应用
- 不需要跨语言通信的场景
- 希望简化开发流程的项目

在本项目中，通过 Redisson 的 `ProtobufCodec`，我们可以轻松使用 Protostuff，无需任何额外配置，只需添加依赖即可。
