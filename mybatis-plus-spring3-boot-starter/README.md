# MyBatis-Plus Spring3 Boot Starter

[![Maven Central](https://img.shields.io/maven-central/v/io.github.archer099/mybatis-plus-spring3-boot-starter.svg?style=flat-square)](https://search.maven.org/artifact/io.github.archer099/mybatis-plus-spring3-boot-starter)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg?style=flat-square)](http://www.apache.org/licenses/LICENSE-2.0.html)

**一句话概述：** 基于 [MyBatis-Plus](https://github.com/baomidou/mybatis-plus) 的增强版 Spring Boot 3 Starter，提供动态表名、多数据源、自动刷新等高级功能。

## 背景

在企业级应用开发中，经常需要处理以下场景：

1. **分表操作**：随着业务增长，单表数据量增大，需要按照一定规则（如时间、地区等）进行分表
2. **多数据源**：系统需要连接多个数据库，如主从库、不同业务库等
3. **配置刷新**：需要在不重启应用的情况下，动态调整数据源配置

传统的 MyBatis 或 MyBatis-Plus 配置这些功能较为复杂，需要编写大量代码。`mybatis-plus-spring3-boot-starter` 基于官方 MyBatis-Plus，提供了更加便捷的配置方式和增强功能，特别适合 Spring Boot 3.x 的应用开发。

## 项目目标

- **简化配置**：通过简单的 YAML 配置即可实现复杂的功能
- **动态表名**：支持基于参数或上下文的动态表名路由
- **多数据源支持**：轻松配置和管理多个数据源
- **配置热刷新**：支持数据源配置热刷新，无需重启应用
- **性能优化**：内置多项性能优化配置，提高查询和批量操作性能
- **透明集成**：与 Spring Boot 3.x 无缝集成，自动配置

## 核心功能与亮点 ✨

- **动态表名**：支持基于线程上下文的动态表名映射，轻松实现分表
- **多数据源**：基于 dynamic-datasource 提供强大的多数据源支持
- **数据源刷新**：支持动态刷新数据源配置，无需重启应用
- **批量插入优化**：内置 `InsertBatchSomeColumn` 方法，高效批量插入
- **自动填充**：支持创建时间和更新时间自动填充
- **SQL 防护**：内置 `BlockAttackInnerInterceptor` 防止全表更新与删除
- **分页优化**：自动配置分页插件，支持多数据库
- **乐观锁**：自动配置乐观锁插件，简化并发控制

## 技术栈 🛠️

- Java 21
- Spring Boot 3.x
- MyBatis-Plus 3.5.x
- Dynamic-Datasource 4.x
- Transmittable Thread Local

## 快速开始 🚀

### 添加依赖

```xml
<dependency>
    <groupId>io.github.archer099</groupId>
    <artifactId>mybatis-plus-spring3-boot-starter</artifactId>
    <version>${latest.version}</version>
</dependency>
```

### 基础配置

在 `application.yml` 中添加基础配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: password
    driver-class-name: com.mysql.cj.jdbc.Driver

mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  type-aliases-package: com.example.domain
  configuration:
    map-underscore-to-camel-case: true
    cache-enabled: false
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
```

### 动态表名配置

配置动态表名规则：

```yaml
mybatis:
  dynamic:
    table-rule:
      user: user_{tenant}  # 表名: 表名模板
      order: order_{year}_{month}
```

使用动态表名：

```java
/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description 用户服务
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    
    /**
     * 查询特定租户的用户
     */
    public List<User> listUsersByTenant(String tenant) {
        // 设置动态表名参数
        RequestDataHelper.setRequestData("tenant", tenant);
        try {
            // 此时会查询 user_{tenant} 表
            return userMapper.selectList(new LambdaQueryWrapper<>());
        } finally {
            // 清除线程变量，防止内存泄漏
            RequestDataHelper.remove();
        }
    }
    
    /**
     * 查询特定月份的订单
     */
    public List<Order> listOrdersByMonth(int year, int month) {
        Map<String, Object> params = new HashMap<>();
        params.put("year", year);
        params.put("month", String.format("%02d", month));
        
        // 设置动态表名参数
        RequestDataHelper.setRequestData(params);
        try {
            // 此时会查询 order_{year}_{month} 表
            return orderMapper.selectList(new LambdaQueryWrapper<>());
        } finally {
            RequestDataHelper.remove();
        }
    }
}
```

### 多数据源配置

配置多数据源：

```yaml
spring:
  datasource:
    dynamic:
      primary: master  # 默认数据源
      strict: false    # 严格匹配数据源，未匹配到是否报错
      datasource:
        master:
          url: jdbc:mysql://localhost:3306/master?useUnicode=true&characterEncoding=utf8
          username: root
          password: password
          driver-class-name: com.mysql.cj.jdbc.Driver
        slave:
          url: jdbc:mysql://localhost:3307/slave?useUnicode=true&characterEncoding=utf8
          username: root
          password: password
          driver-class-name: com.mysql.cj.jdbc.Driver
      # 支持热刷新数据源配置
      refresh:
        enabled: true
```

使用多数据源：

```java
import com.baomidou.dynamic.datasource.annotation.DS;

/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description 用户服务
 */
@Service
public class UserService {

    /**
     * 默认使用主库
     */
    public User getUser(Long id) {
        return userMapper.selectById(id);
    }
    
    /**
     * 显式指定使用从库
     */
    @DS("slave")
    public List<User> listUsers() {
        return userMapper.selectList(null);
    }
    
    /**
     * 动态指定数据源
     */
    public User getUserFromSpecificDb(Long id, String datasource) {
        return DynamicDataSourceContextHolder.push(datasource, () -> {
            return userMapper.selectById(id);
        });
    }
}
```

## 高级用法

### 1. 批量插入

本starter增强了MyBatis-Plus，内置了批量插入方法：

```java
import com.baomidou.mybatisplus.extension.injector.methods.InsertBatchSomeColumn;

/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description 自定义Mapper接口
 */
public interface UserMapper extends BaseMapper<User> {
    
    /**
     * 批量插入
     * EasySqlInjector 已经注入了此方法，可以直接使用
     */
    int insertBatchSomeColumn(List<User> entityList);
}

/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description 用户服务
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    
    /**
     * 批量保存用户
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean batchSaveUsers(List<User> users) {
        return userMapper.insertBatchSomeColumn(users) > 0;
    }
}
```

### 2. 动态数据源刷新

当需要不重启应用而修改数据源配置时，可以使用动态刷新功能：

```java
/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description 数据源管理服务
 */
@Service
@RequiredArgsConstructor
public class DataSourceService {

    private final Environment environment;
    private final DynamicDataSourceProperties properties;
    private final DynamicDataSourceRefresher refresher;
    
    /**
     * 修改数据源配置并刷新
     */
    public void updateDataSource(String name, String url, String username, String password) {
        // 获取当前数据源配置
        Map<String, DataSourceProperty> datasource = properties.getDatasource();
        DataSourceProperty property = datasource.get(name);
        
        if (property == null) {
            property = new DataSourceProperty();
            datasource.put(name, property);
        }
        
        // 更新配置
        property.setUrl(url);
        property.setUsername(username);
        property.setPassword(password);
        
        // 触发刷新
        refresher.refreshDataSourceIfNeeded(properties);
    }
}
```

### 3. 自动填充功能

实体类可以利用自动填充功能：

```java
import com.baomidou.mybatisplus.annotation.*;

/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description 用户实体
 */
@Data
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String username;
    
    private String password;
    
    /**
     * 创建时间自动填充
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间自动填充
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    /**
     * 乐观锁字段
     */
    @Version
    private Integer version;
    
    /**
     * 逻辑删除字段
     */
    @TableLogic
    private Integer deleted;
}
```

### 4. 多租户应用场景

结合动态表名和多数据源，可以实现完整的多租户方案：

```java
/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description 多租户服务
 */
@Service
@RequiredArgsConstructor
public class MultiTenantService {
    
    private final UserMapper userMapper;
    
    /**
     * 数据库隔离的多租户方案
     */
    public User getUserByDatabaseIsolation(String tenant, Long userId) {
        // 动态切换数据源
        return DynamicDataSourceContextHolder.push(tenant, () -> {
            return userMapper.selectById(userId);
        });
    }
    
    /**
     * 表隔离的多租户方案
     */
    public User getUserByTableIsolation(String tenant, Long userId) {
        RequestDataHelper.setRequestData("tenant", tenant);
        try {
            return userMapper.selectById(userId);
        } finally {
            RequestDataHelper.remove();
        }
    }
    
    /**
     * 字段隔离的多租户方案
     */
    public User getUserByColumnIsolation(String tenant, Long userId) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getTenantId, tenant)
               .eq(User::getId, userId);
        return userMapper.selectOne(wrapper);
    }
}
```

## 数据权限控制

本框架提供了灵活的数据权限控制功能，可以基于用户角色、部门等维度进行数据访问控制，确保用户只能访问被授权的数据。

### 1. 配置数据权限

在 `application.yml` 中开启数据权限功能：

```yaml
mybatis:
  data:
    permission:
      enabled: true  # 默认为true，可省略
      sql-log: false # 是否打印权限SQL日志
```

### 2. 使用注解标记需要权限控制的方法

```java
/**
 * @author archer099
 * @date 2023-07-22 15:23:00
 * @description 用户服务
 */
@Service
public class UserService {

    /**
     * 自定义数据权限控制
     * 查询时会自动添加权限过滤条件
     */
    @DataPermission(type = "SELF_AND_SUB", 
                    value = {
                        @DataColumn(name = "dept_id", alias = "u")
                    })
    public List<User> getUsersByDept() {
        return userMapper.selectList(null);
    }
    
    /**
     * 仅查看本人数据
     */
    @DataPermission(type = "SELF", 
                    value = {
                        @DataColumn(name = "create_by", alias = "u")
                    })
    public List<User> getMyCreatedUsers() {
        return userMapper.selectList(null);
    }
    
    /**
     * 忽略数据权限控制的方法
     */
    @DataPermission(ignore = true)
    public List<User> getAllUsers() {
        return userMapper.selectList(null);
    }
}
```

### 3. 实现权限服务接口

需要实现 `UserPermissionService` 接口，提供当前用户的权限数据：

```java
/**
 * @author archer099
 * @date 2023-07-22 15:23:00
 * @description 自定义用户权限服务实现
 */
@Service
@RequiredArgsConstructor
public class CustomUserPermissionServiceImpl implements UserPermissionService<Long> {
    
    private final SecurityService securityService;
    
    @Override
    public Long getCurrentSelfId(DataColumn dataColumn) {
        // 获取当前用户ID
        return securityService.getCurrentUserId();
    }
    
    @Override
    public List<Long> getSelfAndSubIds(DataColumn dataColumn) {
        // 根据列类型返回不同数据
        if ("dept_id".equals(dataColumn.name())) {
            // 返回当前用户所在部门及子部门ID列表
            return securityService.getCurrentUserDeptAndSubDepts();
        }
        
        // 默认返回当前用户ID列表
        return Collections.singletonList(securityService.getCurrentUserId());
    }
    
    @Override
    public boolean isAdmin() {
        // 判断当前用户是否为管理员
        return securityService.isAdmin();
    }
    
    @Override
    public List<Long> getCustomDataScope(DataColumn dataColumn) {
        // 自定义数据范围实现
        return securityService.getCustomDataScope();
    }
}
```

### 4. 数据权限类型

框架内置了以下数据权限类型：

| 类型 | 描述 |
| ---- | ---- |
| ALL | 全部数据权限，不添加过滤条件 |
| SELF | 仅本人数据，添加 "column = 当前用户ID" 条件 |
| SELF_AND_SUB | 本人及下属数据，添加 "column IN (本人及下属ID列表)" 条件 |
| CUSTOM | 自定义数据权限，可以在注解中指定自定义SQL条件 |

### 5. 自定义权限处理器

如果内置的权限类型不能满足需求，可以自定义权限处理器：

```java
/**
 * @author archer099
 * @date 2023-07-22 15:23:00
 * @description 自定义数据权限处理器
 */
@Component
@RequiredArgsConstructor
public class CustomDataPermissionHandler extends AbstractDataPermissionHandler {

    private final UserPermissionService userPermissionService;
    
    @Override
    public String getType() {
        return "CUSTOM_TYPE";
    }
    
    @Override
    public Expression buildCondition(DataColumn dataColumn) {
        // 构建自定义SQL条件
        String columnName = buildColumnName(dataColumn);
        List<Long> ids = customService.getDataIds();
        
        // 返回 column IN (id1, id2, ...)
        return buildInCondition(columnName, ids);
    }
}
```

### 6. 工作原理

数据权限功能通过 MyBatis 拦截器实现，在查询执行前动态修改 SQL，添加权限过滤条件：

1. 拦截 MyBatis 执行的 SQL 查询
2. 检查方法或类上是否有 `@DataPermission` 注解
3. 根据注解中配置的类型和列信息，构建权限过滤条件
4. 将权限条件追加到原始 SQL 的 WHERE 子句中
5. 执行修改后的 SQL，返回符合权限条件的数据

### 7. 最佳实践

- 对查询方法使用 `@DataPermission` 注解，对增删改方法不需要添加
- 管理员用户可以通过 `isAdmin()` 方法判断，跳过权限控制
- 在复杂查询中，使用表别名指定权限控制的列：`@DataColumn(name = "dept_id", alias = "u")`
- 如果某些特殊场景需要查询所有数据，使用 `@DataPermission(ignore = true)` 忽略权限控制
- 在多租户系统中，结合动态表名和数据权限，可以实现更精细的权限控制

## 配置参数详解

### MyBatis-Plus 配置

| 参数 | 描述 | 默认值 |
| ---- | ---- | ------ |
| `mybatis-plus.mapper-locations` | MyBatis Mapper 文件位置 | classpath*:/mapper/**/*.xml |
| `mybatis-plus.type-aliases-package` | 实体类包路径 | - |
| `mybatis-plus.global-config.db-config.id-type` | 主键类型 | auto |
| `mybatis-plus.global-config.db-config.logic-delete-field` | 逻辑删除字段 | deleted |
| `mybatis-plus.global-config.db-config.logic-delete-value` | 逻辑删除值 | 1 |
| `mybatis-plus.global-config.db-config.logic-not-delete-value` | 逻辑未删除值 | 0 |

### 动态表名配置

| 参数 | 描述 | 默认值 |
| ---- | ---- | ------ |
| `mybatis.dynamic.table-rule` | 动态表名规则映射 | - |

### 多数据源配置

| 参数 | 描述 | 默认值 |
| ---- | ---- | ------ |
| `spring.datasource.dynamic.primary` | 主数据源名称 | master |
| `spring.datasource.dynamic.strict` | 严格匹配数据源 | false |
| `spring.datasource.dynamic.datasource` | 数据源配置 | - |
| `spring.datasource.dynamic.refresh.enabled` | 是否启用数据源刷新 | false |

## 最佳实践

### 1. 数据分片策略

根据业务场景选择合适的分片策略：

- **按时间分片**：日志、订单等有明显时间特性的数据
  ```yaml
  mybatis:
    dynamic:
      table-rule:
        order: order_{year}_{month}
        log: log_{date}
  ```

- **按业务分片**：不同业务数据分开存储
  ```yaml
  mybatis:
    dynamic:
      table-rule:
        user: user_{business_type}
  ```

- **按地区分片**：不同地区数据分开存储
  ```yaml
  mybatis:
    dynamic:
      table-rule:
        customer: customer_{region_code}
  ```

### 2. 多数据源规划

合理规划多数据源配置：

- **读写分离**：主库负责写操作，从库负责读操作
  ```yaml
  spring:
    datasource:
      dynamic:
        datasource:
          master: # 主库配置
            # 写操作
          slave_1: # 从库1配置
            # 读操作
          slave_2: # 从库2配置
            # 读操作
  ```

- **业务隔离**：不同业务使用不同数据源
  ```yaml
  spring:
    datasource:
      dynamic:
        datasource:
          user: # 用户业务数据源
          order: # 订单业务数据源
          product: # 产品业务数据源
  ```

### 3. 性能优化建议

- **批量操作**：使用 `insertBatchSomeColumn` 而非循环单条插入
- **分页查询**：设置合理的页大小，避免大量数据查询
- **索引优化**：为查询条件和排序字段创建索引
- **避免全表扫描**：始终使用条件查询，避免全表操作
- **减少传输量**：只查询需要的字段，避免 `SELECT *`

## 常见问题

### 1. 动态表名不生效

可能的原因：
- 未正确设置 RequestDataHelper 参数
- 表名规则配置不匹配
- 线程变量未正确传递

解决方法：
1. 检查 `mybatis.dynamic.table-rule` 配置是否正确
2. 确保在同一线程中设置和使用 RequestDataHelper
3. 调用完成后及时清理 RequestDataHelper

### 2. 多数据源切换失败

可能的原因：
- 数据源名称不匹配
- 数据源配置错误
- AOP代理问题

解决方法：
1. 检查数据源名称是否与配置一致
2. 检查数据源配置是否正确
3. 确保 @DS 注解在代理方法上有效

### 3. 数据源刷新不生效

可能的原因：
- 刷新配置未启用
- 数据源变更不完整
- 数据源创建失败

解决方法：
1. 确认 `spring.datasource.dynamic.refresh.enabled` 设置为 true
2. 确保更新了完整的数据源配置
3. 检查日志，查看刷新过程是否有错误

## 贡献 🙏

欢迎提交Issue或Pull Request参与项目贡献！

## 许可证

本项目使用 [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0.html) 许可证。

## 致谢

本项目基于以下优秀开源项目开发：
- [MyBatis-Plus](https://github.com/baomidou/mybatis-plus)
- [Dynamic-Datasource](https://github.com/baomidou/dynamic-datasource-spring-boot-starter)

感谢这些项目团队提供的优秀开源工具！ 