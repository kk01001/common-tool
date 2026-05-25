# Redis 序列化方式对比示例

这个示例项目演示了如何使用 `multi-redis-spring-boot-starter` 的不同序列化方式，并对比它们的性能和内存占用。

## 支持的序列化方式

1. **STRING** - 字符串序列化（默认）
2. **JSON** - JSON 格式序列化（使用 Jackson）
3. **PROTOBUF** - Protostuff 二进制序列化（Redisson 内置，无需定义 proto 文件）
4. **KRYO** - Kryo 二进制序列化
5. **FST** - Fast Serialization 序列化

## 快速开始

### 1. 配置 Redis

在 `application.yml` 中配置 Redis 连接：

```yaml
spring:
  data:
    redis:
      codec-type: STRING  # 可选: STRING, JSON, PROTOBUF, KRYO, FST
      cluster:
        nodes:
          - localhost:6379
```

### 2. 启动应用

```bash
mvn spring-boot:run
```

### 3. 访问测试页面

打开浏览器访问: http://localhost:8080

点击"开始测试"按钮，系统会自动测试所有序列化方式并显示对比结果。

## API 接口

### 测试所有序列化方式

```
GET /api/serialization/test
```

返回示例：

```json
[
  {
    "codecType": "STRING",
    "memorySizeBytes": 245,
    "memorySizeReadable": "245 B",
    "serializationTimeMs": 5,
    "deserializationTimeMs": 3,
    "success": true,
    "errorMessage": null
  },
  {
    "codecType": "JSON",
    "memorySizeBytes": 230,
    "memorySizeReadable": "230 B",
    "serializationTimeMs": 4,
    "deserializationTimeMs": 2,
    "success": true,
    "errorMessage": null
  }
]
```

## 性能对比

一般来说，不同序列化方式的特点：

### 内存占用（从小到大）
1. **Protobuf** - 最紧凑的二进制格式
2. **Kryo** - 高效的二进制序列化
3. **FST** - 快速序列化，稍大
4. **JSON** - 文本格式，可读性好
5. **STRING** - JSON 字符串，最大

### 序列化速度（从快到慢）
1. **FST** - 专为速度优化
2. **Kryo** - 快速且紧凑
3. **Protobuf** - 平衡性能和大小
4. **JSON** - 中等速度
5. **STRING** - 需要额外的 JSON 转换

### 反序列化速度（从快到慢）
1. **FST** - 最快
2. **Kryo** - 很快
3. **Protobuf** - 快速
4. **JSON** - 中等
5. **STRING** - 需要额外的 JSON 解析

## 选择建议

- **STRING**: 适合简单场景，易于调试
- **JSON**: 适合需要可读性和跨语言的场景
- **Protobuf**: 适合对内存敏感的场景
- **Kryo**: 适合 Java 应用，平衡性能和大小
- **FST**: 适合对速度要求极高的场景

## 注意事项

1. **Protobuf 实现说明**
   - 本项目使用 Redisson 内置的 `ProtobufCodec`
   - 实际使用的是 **Protostuff** 框架，而非 Google 的 Protocol Buffers
   - **优势**：无需定义 `.proto` 文件，自动序列化 Java 对象
   - **依赖**：需要 `protostuff-runtime` 依赖

2. **FST 在 Java 21+ 上的限制**
   - FST 需要访问 JDK 内部类，在 Java 21+ 上会失败
   - **解决方案**：添加 JVM 参数 `--add-opens java.base/java.lang=ALL-UNNAMED`
   - **建议**：生产环境使用 Kryo 或 Protobuf 替代 FST
   - 测试页面会自动检测并跳过 FST 测试

3. **性能影响因素**
   - 不同序列化方式的性能会受数据结构影响
   - 实际性能需要在生产环境中测试
   - 考虑序列化方式的兼容性和可维护性

4. **调试建议**
   - Protobuf、Kryo、FST 是二进制格式，不易调试
   - 开发环境建议使用 STRING 或 JSON
   - 生产环境根据性能需求选择

## 依赖说明

```xml
<dependency>
    <groupId>io.github.archer099</groupId>
    <artifactId>multi-redis-spring-boot-starter</artifactId>
    <version>2.5.0</version>
</dependency>
```

### 序列化依赖

Starter 中的序列化依赖都是 `optional`，使用时需要显式引入：

#### Protobuf (Protostuff)
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

#### Kryo
```xml
<dependency>
    <groupId>com.esotericsoftware</groupId>
    <artifactId>kryo</artifactId>
</dependency>
```

#### FST
```xml
<dependency>
    <groupId>de.ruedigermoeller</groupId>
    <artifactId>fst</artifactId>
</dependency>
```

**注意**：STRING 和 JSON 方式无需额外依赖，Redisson 已内置。
