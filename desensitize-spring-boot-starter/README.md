# 数据脱敏 Spring Boot Starter

[![Maven Central](https://img.shields.io/maven-central/v/io.github.archer099/desensitize-spring-boot-starter.svg?style=flat-square)](https://search.maven.org/artifact/io.github.archer099/desensitize-spring-boot-starter)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg?style=flat-square)](http://www.apache.org/licenses/LICENSE-2.0.html)

**一句话概述：** 轻量级、易扩展的数据脱敏解决方案，支持多种脱敏策略，兼容主流JSON框架，通过简单注解即可保护敏感数据安全。

## 背景

在企业应用开发中，数据脱敏是保护用户隐私和符合数据安全法规的重要措施。常见的敏感数据如手机号、身份证号、银行卡号等，在日志记录、接口返回、数据展示等场景中都需要进行适当脱敏处理。

`desensitize-spring-boot-starter` 提供了简单易用的注解式解决方案，支持多种脱敏类型和自定义扩展，帮助开发者快速实现数据脱敏功能，同时兼容主流JSON序列化框架（Jackson、FastJson），确保数据在传输和展示过程中的安全性。

## 项目目标

- **简化开发**：通过注解方式实现数据脱敏，最小化代码侵入性
- **内置策略**：预置多种常见数据脱敏策略，涵盖常见敏感数据类型
- **灵活扩展**：支持自定义脱敏处理器和脱敏规则
- **多框架兼容**：同时支持Jackson和FastJson两种主流JSON框架
- **高性能**：优化脱敏算法，降低性能影响

## 核心功能与亮点 ✨

- **丰富的脱敏类型**：支持手机号、邮箱、身份证号、银行卡号、姓名等多种敏感信息脱敏
- **注解驱动**：通过 `@Desensitize` 注解即可实现字段脱敏
- **多框架适配**：
  - Jackson：自定义序列化器，支持POJO对象序列化时脱敏
  - FastJson：自定义ValueFilter，支持序列化过程中的数据脱敏
- **自定义扩展**：支持自定义脱敏处理器和脱敏规则
- **脱敏工具类**：提供 `DesensitizeUtil` 工具类，支持手动脱敏处理
- **配置灵活**：支持通过配置文件启用/禁用脱敏功能

## 技术栈 🛠️

- Java 21
- Spring Boot 3.x
- Jackson
- FastJson
- Lombok
- Hutool

## 快速开始 🚀

### 添加依赖

```xml
<dependency>
    <groupId>io.github.archer099</groupId>
    <artifactId>desensitize-spring-boot-starter</artifactId>
    <version>${latest.version}</version>
</dependency>
```

### 配置属性

在 `application.yml` 或 `application.properties` 中启用脱敏功能：

```yaml
desensitize:
  enabled: true  # 启用脱敏功能
```

### 基本使用

1. 在实体类字段上添加 `@Desensitize` 注解：

```java
/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description 用户实体类
 */
@Data
public class UserDTO {
    private Long id;
    
    @Desensitize(type = DesensitizeType.NAME)
    private String name;
    
    @Desensitize(type = DesensitizeType.PHONE)
    private String phone;
    
    @Desensitize(type = DesensitizeType.EMAIL)
    private String email;
    
    @Desensitize(type = DesensitizeType.ID_CARD)
    private String idCard;
    
    @Desensitize(type = DesensitizeType.BANK_CARD)
    private String bankCard;
    
    private Integer age;
    
    private String address;
}
```

2. 在接口中返回对象，自动完成脱敏：

```java
/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description 用户控制器
 */
@RestController
@RequestMapping("/users")
public class UserController {
    
    @GetMapping("/{id}")
    public UserDTO getUserById(@PathVariable Long id) {
        // 从数据库获取用户信息
        UserDTO user = userService.getUserById(id);
        // 返回时会自动脱敏（依赖于Jackson或FastJson的序列化过程）
        return user;
    }
}
```

### 使用自定义脱敏规则

通过注解参数自定义脱敏规则：

```java
/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description 订单实体类
 */
@Data
public class OrderDTO {
    private String orderNo;
    
    // 定制脱敏规则：保留前4位和后4位，中间替换为*
    @Desensitize(type = DesensitizeType.BANK_CARD, startIndex = 4, endIndex = -4, maskChar = "*")
    private String paymentCardNo;
    
    // 自定义脱敏处理器
    @Desensitize(type = DesensitizeType.CUSTOM, handler = MyCustomDesensitizeHandler.class)
    private String customField;
    
    private BigDecimal amount;
}
```

### 手动进行脱敏处理

使用 `DesensitizeUtil` 工具类进行手动脱敏处理：

```java
/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description 日志服务
 */
@Service
@Slf4j
public class LogService {
    
    @Autowired
    private DesensitizeUtil desensitizeUtil;
    
    public void logUserActivity(UserDTO user) {
        // 将对象转换为脱敏后的JSON字符串
        String sensitiveJson = desensitizeUtil.toJson(user);
        // 记录脱敏后的日志
        log.info("用户活动: {}", sensitiveJson);
    }
}
```

## 脱敏类型详解

| 脱敏类型 | 描述 | 示例输入 | 示例输出 |
|--------|------|---------|---------|
| PHONE | 手机号码，保留前3后4 | 13812345678 | 138****5678 |
| EMAIL | 邮箱，隐藏@前面部分 | user@example.com | u***@example.com |
| NAME | 姓名，隐藏中间字符 | 张三丰 | 张*丰 |
| ID_CARD | 身份证号，保留前6后4 | 110101199001011234 | 110101********1234 |
| BANK_CARD | 银行卡号，保留前4后4 | 6225123412341234 | 6225********1234 |
| ADDRESS | 地址，隐藏详细地址 | 北京市海淀区中关村南大街5号 | 北京市海淀区******* |
| PASSWORD | 密码，全部脱敏 | password123 | *********** |
| CAR_NUMBER | 车牌号，隐藏部分字符 | 京A12345 | 京A*** |
| FIXED_PHONE | 固定电话，保留区号 | 010-12345678 | 010-**** |
| IPV4 | IPv4地址，隐藏最后一段 | 192.168.1.1 | 192.168.1.* |
| IPV6 | IPv6地址，隐藏部分段 | 2001:0db8:85a3:0000:0000:8a2e:0370:7334 | 2001:0db8:****:****:****:****:****:7334 |
| PASSPORT | 护照号码，隐藏部分字符 | G12345678 | G**** |
| MILITARY_ID | 军官证号，隐藏部分字符 | 军123456 | 军**** |
| CNAPS_CODE | 公司开户银行联号，隐藏部分 | 123456789012 | 1234****** |
| DOMAIN | 域名，隐藏一级域名前缀 | api.example.com | ***.example.com |
| MASK_ALL | 全部字符脱敏 | 任意内容 | ******* |
| CUSTOM | 自定义脱敏规则 | - | - |

## 自定义脱敏处理器

1. 实现 `DesensitizeHandler` 接口：

```java
/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description 自定义脱敏处理器
 */
@Component
@DesensitizeFor(DesensitizeType.CUSTOM)  // 指定处理器对应的脱敏类型
public class MyCustomDesensitizeHandler implements DesensitizeHandler {
    
    @Override
    public String desensitize(String value, Desensitize annotation) {
        if (value == null || value.length() == 0) {
            return value;
        }
        // 自定义脱敏逻辑
        return value.substring(0, 1) + "***" + value.substring(value.length() - 1);
    }
    
    @Override
    public String desensitize(String value) {
        return desensitize(value, null);
    }
}
```

2. 或者继承 `AbstractDesensitizeHandler` 类：

```java
/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description 海外手机号脱敏处理器
 */
@Component
@DesensitizeFor(DesensitizeType.CUSTOM)
public class OverseasPhoneDesensitizeHandler extends AbstractDesensitizeHandler {
    
    @Override
    public String doDesensitize(String value, int startIndex, int endIndex, String maskChar) {
        // 国际手机号格式（如: +86 13812345678）
        int plusIndex = value.indexOf("+");
        if (plusIndex >= 0) {
            String countryCode = value.substring(0, value.indexOf(" ", plusIndex));
            String phoneNumber = value.substring(value.indexOf(" ", plusIndex) + 1);
            
            // 对手机号部分进行脱敏
            String maskedPhoneNumber = super.doDesensitize(phoneNumber, 3, phoneNumber.length() - 4, maskChar);
            return countryCode + " " + maskedPhoneNumber;
        }
        
        // 如果不是国际格式，使用普通手机号脱敏方式
        return super.doDesensitize(value, 3, value.length() - 4, maskChar);
    }
}
```

## JSON框架集成

### Jackson集成

默认已配置，无需额外操作。如需定制：

```java
/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description Jackson配置类
 */
@Configuration
public class JacksonConfig {
    
    @Bean
    public ObjectMapper objectMapper(DesensitizeHandlerFactory desensitizeHandlerFactory) {
        ObjectMapper objectMapper = new ObjectMapper();
        
        // 注册脱敏模块
        SimpleModule module = new SimpleModule();
        module.addSerializer(String.class, new DesensitizeSerializer(desensitizeHandlerFactory));
        objectMapper.registerModule(module);
        
        return objectMapper;
    }
}
```

### FastJson集成

默认已配置，无需额外操作。如需定制：

```java
/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description FastJson配置类
 */
@Configuration
public class FastjsonConfig {
    
    @Bean
    public HttpMessageConverters fastJsonHttpMessageConverters(DesensitizeHandlerFactory desensitizeHandlerFactory) {
        // 创建FastJson配置
        FastJsonConfig config = new FastJsonConfig();
        
        // 添加脱敏过滤器
        ValueFilter desensitizeFilter = new DesensitizeValueFilter(desensitizeHandlerFactory);
        config.setSerializeFilters(desensitizeFilter);
        
        // 创建FastJson消息转换器
        FastJsonHttpMessageConverter converter = new FastJsonHttpMessageConverter();
        converter.setFastJsonConfig(config);
        
        return new HttpMessageConverters(converter);
    }
}
```

## 最佳实践

1. **合理选择脱敏类型**：根据数据的敏感程度和业务需求选择合适的脱敏类型
2. **自定义脱敏参数**：对于特殊格式的数据，可以通过自定义脱敏参数提高脱敏效果
3. **性能优化**：对于大量数据的场景，可以考虑使用缓存或批量处理
4. **组合使用**：结合Spring Security等安全框架，提供更全面的数据保护方案
5. **动态配置**：根据环境或用户权限动态决定是否启用脱敏

## 应用场景

- **API响应脱敏**：接口返回给前端的敏感信息自动脱敏
- **日志脱敏**：记录日志时对敏感信息进行脱敏处理
- **数据导出脱敏**：导出报表或文件时对敏感数据进行脱敏
- **测试数据准备**：为测试环境准备脱敏后的真实数据
- **数据展示脱敏**：在管理后台或客户端展示用户信息时进行部分脱敏

## 贡献 🙏

欢迎提交 Issue 或 Pull Request 参与项目贡献！

## 许可证

本项目使用 [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0.html) 许可证。 