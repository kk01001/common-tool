# 字典翻译 Spring Boot Starter

[![Maven Central](https://img.shields.io/maven-central/v/io.github.archer099/dict-spring-boot-starter.svg?style=flat-square)](https://search.maven.org/artifact/io.github.archer099/dict-spring-boot-starter)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg?style=flat-square)](http://www.apache.org/licenses/LICENSE-2.0.html)

**一句话概述：** 简化开发的字典翻译工具，支持注解驱动自动翻译，高性能缓存机制提升效率，适用于Java后端的各类字典代码转文本场景。

## 背景

在企业应用开发中，我们经常需要处理各种字典数据，如性别编码（1-男，2-女）、状态码（0-禁用，1-启用）等。当前端展示这些数据时，通常需要将编码转换为对应的文本描述。传统实现方式需要在每个DTO或VO类中手动添加额外的字段，代码冗余且容易出错。

`dict-spring-boot-starter` 提供了一种优雅的解决方案，通过简单的注解即可自动完成字典编码到文本的转换，大大简化了开发过程，提高了代码的可维护性。

## 项目目标

- **简化开发**：通过注解快速完成字典翻译，减少冗余代码
- **高性能**：基于内存缓存实现，避免重复数据库查询
- **扩展性强**：支持自定义字典加载方式和数据源
- **零侵入**：基于Jackson序列化扩展，对现有代码无侵入
- **易于集成**：最小化配置即可使用

## 核心功能与亮点 ✨

- **注解驱动**：使用`@Dict`注解即可自动翻译字典项
- **缓存机制**：内置高性能字典缓存，避免重复加载
- **多种数据源**：支持自定义字典加载器，可从数据库、Redis、配置文件等加载字典数据
- **动态翻译**：序列化时自动添加翻译字段，无需修改现有代码
- **自动刷新**：支持字典数据自动刷新/定时刷新

## 技术栈 🛠️

- Java 21
- Spring Boot 3.x
- Jackson (JSON序列化)
- Spring JDBC

## 快速开始 🚀

### 添加依赖

```xml
<dependency>
    <groupId>io.github.archer099</groupId>
    <artifactId>dict-spring-boot-starter</artifactId>
    <version>${latest.version}</version>
</dependency>
```

### 配置属性

在`application.yml`或`application.properties`中添加以下配置：

```yaml
dict:
  # 是否启用字典功能，默认关闭
  enabled: true
  # 是否在应用启动时自动刷新字典缓存，默认开启
  auto-refresh: true
```

### 实现字典加载器

创建一个实现`DictLoader`接口的类，用于加载字典数据：

```java
/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description 数据库字典加载器
 */
@Component
@RequiredArgsConstructor
public class DatabaseDictLoader implements DictLoader {
    
    private final JdbcTemplate jdbcTemplate;
    
    @Override
    public Map<String, String> loadDict(String type) {
        // 从数据库加载特定类型的字典
        String sql = "SELECT dict_value, dict_text FROM sys_dict_item WHERE dict_type = ?";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, type);
        
        Map<String, String> result = new HashMap<>();
        for (Map<String, Object> item : list) {
            result.put(String.valueOf(item.get("dict_value")), 
                       String.valueOf(item.get("dict_text")));
        }
        return result;
    }
    
    @Override
    public Map<String, Map<String, String>> loadAllDict() {
        // 加载所有字典
        String sql = "SELECT dict_type, dict_value, dict_text FROM sys_dict_item";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
        
        Map<String, Map<String, String>> result = new HashMap<>();
        for (Map<String, Object> item : list) {
            String type = String.valueOf(item.get("dict_type"));
            String value = String.valueOf(item.get("dict_value"));
            String text = String.valueOf(item.get("dict_text"));
            
            result.computeIfAbsent(type, k -> new HashMap<>())
                  .put(value, text);
        }
        return result;
    }
}
```

### 在实体类中使用

在需要翻译的字段上添加`@Dict`注解：

```java
/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description 用户信息VO
 */
@Data
public class UserVO {
    private Long id;
    private String username;
    
    /**
     * 性别: 1-男，2-女
     */
    @Dict("sex")
    private String sex;
    
    /**
     * 状态: 0-禁用，1-启用
     */
    @Dict(value = "user_status", suffix = "Name")
    private String status;
    
    /**
     * 会员等级: 1-普通，2-黄金，3-白金
     */
    @Dict(value = "vip_level", table = "t_vip", field = "name")
    private String vipLevel;
    
    private Date createTime;
}
```

当这个实体类通过控制器返回并序列化为JSON时，将自动添加翻译字段：

```json
{
  "id": 1,
  "username": "zhang.san",
  "sex": "1",
  "sexText": "男",
  "status": "1", 
  "statusName": "启用",
  "vipLevel": "2",
  "vipLevelText": "黄金",
  "createTime": "2023-01-01T12:00:00"
}
```

## 字典注解参数详解

`@Dict`注解提供以下参数配置：

| 参数 | 说明 | 默认值 | 示例 |
| --- | --- | --- | --- |
| value | 字典类型/编码，必填 | 无 | `@Dict("sex")` |
| suffix | 翻译后的字段后缀 | Text | `@Dict(value="status", suffix="Name")` |
| table | 指定查询的表名 | "" | `@Dict(value="type", table="t_config")` |
| field | 指定查询的字段名 | "" | `@Dict(value="type", table="t_config", field="name")` |

说明：
- 当指定`table`和`field`时，如果缓存中没有找到对应字典，将直接查询数据库
- `suffix`用于控制生成的翻译字段后缀，例如将`status`翻译为`statusName`

## 字典加载策略

字典数据加载遵循以下优先级策略：

1. 首先从内存缓存中查询
2. 如果缓存未命中，通过`DictLoader`接口加载对应类型的字典
3. 如果`DictLoader`未能加载到字典且指定了`table`和`field`，直接从数据库查询
4. 如果都未能找到，返回`null`

## 缓存刷新机制

字典缓存支持以下刷新方式：

### 1. 启动时自动刷新

通过配置`dict.auto-refresh=true`启用应用启动时自动刷新缓存。

### 2. 手动刷新

可以注入`DictRefresher`手动触发字典刷新：

```java
/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description 字典管理服务
 */
@Service
@RequiredArgsConstructor
public class DictService {
    
    private final DictRefresher dictRefresher;
    
    /**
     * 刷新全部字典
     */
    public void refreshAllDict() {
        // 刷新所有字典缓存
        DictCache.refreshAll();
    }
    
    /**
     * 刷新指定类型字典
     */
    public void refreshDict(String type, Map<String, String> dict) {
        // 刷新特定类型的字典缓存
        DictCache.refresh(type, dict);
    }
}
```

### 3. 定时刷新

结合Spring的定时任务功能实现定时刷新：

```java
/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description 字典定时刷新任务
 */
@Component
public class DictRefreshTask {
    
    /**
     * 每小时刷新一次字典缓存
     */
    @Scheduled(fixedRate = 3600000)
    public void refreshDictTask() {
        DictCache.refreshAll();
    }
}
```

## 自定义字典加载器

除了默认的数据库加载器，您还可以实现自定义的字典加载器，例如从Redis、配置文件或其他数据源加载字典：

### Redis字典加载器示例

```java
/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description Redis字典加载器
 */
@Component
@RequiredArgsConstructor
public class RedisBasedDictLoader implements DictLoader {
    
    private final StringRedisTemplate redisTemplate;
    
    @Override
    public Map<String, String> loadDict(String type) {
        // 从Redis中加载字典数据
        // 假设字典以Hash结构存储, key为dict:type
        Map<Object, Object> entries = redisTemplate.opsForHash().entries("dict:" + type);
        
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            result.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
        }
        return result;
    }
    
    @Override
    public Map<String, Map<String, String>> loadAllDict() {
        // 获取所有字典类型
        Set<String> keys = redisTemplate.keys("dict:*");
        if (keys == null || keys.isEmpty()) {
            return Collections.emptyMap();
        }
        
        Map<String, Map<String, String>> result = new HashMap<>();
        for (String key : keys) {
            String type = key.substring(5); // 去掉前缀"dict:"
            Map<String, String> dict = loadDict(type);
            result.put(type, dict);
        }
        return result;
    }
}
```

## 高级用法

### 1. 指定表字段查询

当某些字典项不是通过通用字典表维护，而是其他业务表中的数据时，可以使用表字段查询：

```java
/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description 订单VO
 */
@Data
public class OrderVO {
    private Long orderId;
    
    /**
     * 用户ID，直接查询用户表
     */
    @Dict(value = "user_id", table = "t_user", field = "username")
    private Long userId;
    
    /**
     * 商品ID，直接查询商品表
     */
    @Dict(value = "product_id", table = "t_product", field = "product_name")
    private Long productId;
    
    private BigDecimal amount;
}
```

### 2. 手动获取字典文本

在某些场景下，可能需要手动获取字典文本，可以直接使用`DictCache`：

```java
/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description 服务类中手动获取字典文本
 */
@Service
public class ProductService {
    
    public String getProductStatusName(String statusCode) {
        // 手动获取字典文本
        return DictCache.getDictText("product_status", statusCode, "", "");
    }
    
    public void processOrders(List<Order> orders) {
        for (Order order : orders) {
            // 处理订单状态翻译
            String statusText = DictCache.getDictText("order_status", order.getStatus(), "", "");
            // 其他业务逻辑...
        }
    }
}
```

## 最佳实践

1. **性能考虑**
   - 尽量提前加载所有字典数据到缓存
   - 对于大型字典，考虑按需加载策略
   - 合理设置缓存刷新频率，避免频繁刷新

2. **数据结构**
   - 推荐使用专门的字典表管理字典数据
   ```sql
   CREATE TABLE sys_dict_item (
       id BIGINT PRIMARY KEY,
       dict_type VARCHAR(100) NOT NULL COMMENT '字典类型',
       dict_value VARCHAR(100) NOT NULL COMMENT '字典值',
       dict_text VARCHAR(100) NOT NULL COMMENT '字典文本',
       sort INT DEFAULT 0 COMMENT '排序',
       status CHAR(1) DEFAULT '1' COMMENT '状态(0-禁用,1-启用)',
       create_time DATETIME,
       update_time DATETIME,
       UNIQUE KEY uk_dict_type_value (dict_type, dict_value)
   ) COMMENT '字典数据表';
   ```

3. **字典管理**
   - 提供完善的字典管理界面
   - 支持字典的CRUD操作
   - 实现字典缓存手动刷新功能

## 应用场景

- **用户界面**：将编码转换为可读文本显示
- **数据导出**：Excel、PDF等报表导出时进行字典翻译
- **API接口**：返回给客户端的数据自动翻译
- **国际化**：结合国际化功能，实现多语言字典翻译

## 贡献 🙏

欢迎提交Issue或Pull Request参与项目贡献！

## 许可证

本项目使用 [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0.html) 许可证。 