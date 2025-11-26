# MyBatis Plus 示例应用

这是一个完整的 MyBatis Plus 示例应用,演示了 `mybatis-plus-spring3-boot-starter` 的核心功能。

## 📚 功能特性

### 核心功能
- ✅ **CRUD 操作** - 完整的增删改查功能
- ✅ **分页查询** - 基于 MyBatis Plus 的分页插件
- ✅ **条件查询** - Lambda 条件构造器
- ✅ **逻辑删除** - 自动处理逻辑删除
- ✅ **自动填充** - 创建时间和更新时间自动填充
- ✅ **数据权限** - 基于部门的数据权限控制

### 高级功能
- ✅ **SQL 注入器** - 使用 starter 提供的批量插入方法
- ✅ **流式查询** - 大数据量处理,避免内存溢出
- ✅ **乐观锁** - 并发控制,防止数据覆盖
- ✅ **枚举转换** - 枚举类型自动转换
- ✅ **批量操作** - 高效的批量插入

### 技术栈
- Spring Boot 3.x
- MyBatis Plus 3.x
- H2 Database (内存数据库)
- Lombok
- Hutool

## 🚀 快速开始

### 1. 编译项目

```bash
mvn clean compile -pl examples-starter/mybatis-plus-example
```

### 2. 启动应用

```bash
mvn spring-boot:run -pl examples-starter/mybatis-plus-example
```

### 3. 访问应用

打开浏览器访问: http://localhost:8083

## 📖 功能页面

### 主页
- 地址: http://localhost:8083/
- 功能: 应用介绍和导航

### 用户管理系统
- 地址: http://localhost:8083/user-management.html
- 功能:
  - 用户列表展示(分页)
  - 添加新用户
  - 编辑用户信息
  - 删除用户
  - 搜索用户(按用户名或邮箱)
  - 按部门筛选

### 数据权限演示
- 地址: http://localhost:8083/data-permission.html
- 功能:
  - 按部门过滤数据
  - 数据权限配置说明
  - 代码示例展示

### H2 数据库控制台
- 地址: http://localhost:8083/h2-console
- JDBC URL: `jdbc:h2:mem:testdb`
- 用户名: `sa`
- 密码: (留空)

## 🔧 API 接口

### 用户管理 API

#### 分页查询用户
```http
GET /api/users?current=1&size=10
```

#### 根据 ID 查询用户
```http
GET /api/users/{id}
```

#### 创建用户
```http
POST /api/users
Content-Type: application/json

{
  "username": "张三",
  "email": "zhangsan@example.com",
  "age": 25,
  "department": "技术部"
}
```

#### 更新用户
```http
PUT /api/users/{id}
Content-Type: application/json

{
  "username": "张三",
  "email": "zhangsan@example.com",
  "age": 26,
  "department": "技术部"
}
```

#### 删除用户
```http
DELETE /api/users/{id}
```

#### 搜索用户
```http
GET /api/users/search?keyword=张三
```

#### 按部门查询用户
```http
GET /api/users/department/技术部
```

## 📝 数据库设计

### 用户表 (user)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键(自增) |
| username | VARCHAR(50) | 用户名 |
| email | VARCHAR(100) | 邮箱 |
| age | INT | 年龄 |
| department | VARCHAR(50) | 部门 |
| create_time | TIMESTAMP | 创建时间 |
| update_time | TIMESTAMP | 更新时间 |
| deleted | INT | 逻辑删除标志 |

## 💡 核心配置

### application.yml

```yaml
# MyBatis Plus 配置
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
```

### 实体类注解

```java
@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String username;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}
```

## 🎯 MyBatis Plus 特性演示

### 1. Lambda 条件构造器

```java
LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
wrapper.like(User::getUsername, keyword)
       .or()
       .like(User::getEmail, keyword);
List<User> users = userMapper.selectList(wrapper);
```

### 2. 分页查询

```java
Page<User> page = new Page<>(current, size);
Page<User> result = userMapper.selectPage(page, null);
```

### 3. 逻辑删除

```java
// 删除操作会自动更新 deleted 字段为 1
userMapper.deleteById(id);

// 查询时自动过滤 deleted = 1 的记录
userMapper.selectList(null);
```

### 4. 自动填充

```java
// 插入时自动填充 createTime
// 更新时自动填充 updateTime
userMapper.insert(user);
userMapper.updateById(user);
```

### 5. 批量插入(使用 Starter 提供的方法)

本示例使用 `mybatis-plus-spring3-boot-starter` 提供的批量插入功能,无需自定义 SQL 注入器。

**Starter 已配置:**
- `EasySqlInjector` - 提供 `insertBatchSomeColumn` 方法
- `OptimisticLockerInnerInterceptor` - 乐观锁插件
- `PaginationInnerInterceptor` - 分页插件
- `BlockAttackInnerInterceptor` - 防全表更新删除
- `DynamicTableNameInnerInterceptor` - 动态表名

**使用方法:**

```java
// 1. Mapper 继承 CustomBaseMapper
public interface ProductMapper extends CustomBaseMapper<Product> {
}

// 2. 直接调用批量插入方法
List<Product> products = generateTestData(100);
int rows = productMapper.insertBatchSomeColumn(products);
```

**性能对比:**
- 单条插入 100 条: ~1000ms
- 批量插入 100 条: ~50ms (性能提升 20 倍)

### 6. 流式查询

使用 `ResultHandler` 处理大数据量,避免内存溢出:

```java
productMapper.selectList(new LambdaQueryWrapper<>(), new ResultHandler<Product>() {
    @Override
    public void handleResult(ResultContext<? extends Product> resultContext) {
        Product product = resultContext.getResultObject();
        // 逐条处理数据
    }
});
```

### 7. 乐观锁

使用 `@Version` 注解实现并发控制:

```java
@Data
public class Product {
    @Version
    private Integer version;
    // 其他字段...
}

// 更新时自动检查版本号
product.setStock(100);
productMapper.updateById(product); // 版本号不匹配时更新失败
```

### 8. 枚举自动转换

使用 `@EnumValue` 注解实现枚举转换:

```java
@Getter
public enum ProductStatus {
    AVAILABLE(1, "在售"),
    OUT_OF_STOCK(2, "缺货"),
    DISCONTINUED(3, "停售");

    @EnumValue
    private final Integer code;
    
    @JsonValue
    private final String description;
}

```

## 📦 项目结构

```
mybatis-plus-example/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── io/github/kk01001/example/mybatis/
│   │   │       ├── MybatisPlusExampleApplication.java
│   │   │       ├── controller/
│   │   │       │   └── UserController.java
│   │   │       ├── entity/
│   │   │       │   └── User.java
│   │   │       ├── mapper/
│   │   │       │   └── UserMapper.java
│   │   │       └── service/
│   │   │           ├── UserService.java
│   │   │           └── impl/
│   │   │               └── UserServiceImpl.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── schema.sql
│   │       ├── data.sql
│   │       └── static/
│   │           ├── index.html
│   │           ├── user-management.html
│   │           └── data-permission.html
│   └── test/
└── pom.xml
```

## 🔍 学习要点

1. **BaseMapper 接口** - 提供基础的 CRUD 方法
2. **条件构造器** - 灵活构建查询条件
3. **分页插件** - 简化分页查询
4. **注解配置** - 使用注解配置实体映射
5. **自动填充** - 自动处理公共字段
6. **逻辑删除** - 软删除实现

## 📄 许可证

本项目采用 Apache License 2.0 许可证。

## 👨‍💻 作者

kk01001
