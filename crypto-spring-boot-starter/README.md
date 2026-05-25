# 加密解密 Spring Boot Starter

[![Maven Central](https://img.shields.io/maven-central/v/io.github.archer099/crypto-spring-boot-starter.svg?style=flat-square)](https://search.maven.org/artifact/io.github.archer099/crypto-spring-boot-starter)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg?style=flat-square)](http://www.apache.org/licenses/LICENSE-2.0.html)

**一句话概述：** 多种加密算法支持的轻量级加密解密框架，支持接口参数加解密、数据库字段加解密，自动集成 Web 和 MyBatis，安全保护敏感数据。

## 背景

在企业应用开发中，数据安全是一个核心问题。无论是传输中的数据还是存储的数据，都可能包含敏感信息（如身份证号、手机号、银行卡号等），这些数据需要进行加密保护。

`crypto-spring-boot-starter` 提供了一套完整的加密解密解决方案，支持多种加密算法（AES、RSA、SM2、SM4），并无缝集成到 Spring MVC 和 MyBatis，实现请求/响应参数和数据库字段的自动加解密，简化开发者的工作，提高数据安全性。

## 项目目标

- **多算法支持**：支持对称加密（AES、SM4）和非对称加密（RSA、SM2）
- **自动加解密**：通过注解自动处理 HTTP 请求/响应和数据库字段的加解密
- **配置灵活**：支持配置文件配置、注解配置和自定义配置
- **易于使用**：最小侵入性设计，简单注解即可使用
- **性能优化**：针对加解密过程进行性能优化
- **国密算法**：支持国产密码算法 SM2/SM4

## 核心功能与亮点 ✨

- **多种加密算法**：
  - **AES**：高效的对称加密算法
  - **RSA**：经典的非对称加密算法
  - **SM2**：国产非对称加密算法
  - **SM4**：国产对称加密算法
- **多场景支持**：
  - **API接口加解密**：自动处理 HTTP 请求和响应的加解密
  - **数据库字段加解密**：自动处理数据库字段的加解密，支持 MyBatis 和 MyBatis-Plus
- **注解驱动**：
  - `@ParamsCrypto`：标记需要加解密的接口方法
  - `@CryptoField`：标记需要加解密的实体类字段
- **配置刷新**：支持配置热更新，动态刷新加密密钥
- **密钥生成**：提供工具方法生成各种类型的密钥

## 技术栈 🛠️

- Java 21
- Spring Boot 3.x
- MyBatis/MyBatis-Plus
- BouncyCastle (国密算法)
- Hutool
- Jackson

## 快速开始 🚀

### 添加依赖

```xml
<dependency>
    <groupId>io.github.archer099</groupId>
    <artifactId>crypto-spring-boot-starter</artifactId>
    <version>${latest.version}</version>
</dependency>
```

### 配置属性

在 `application.yml` 或 `application.properties` 中配置加密参数：

```yaml
common:
  params:
    crypto:
      # 加密类型：AES、RSA、SM2、SM4
      type: AES
      # 对称加密密钥（AES、SM4使用）
      key: Ab123456AbcDefGh
      # RSA/SM2公钥（非对称加密使用）
      public-key: MIGfMA0GCSqGSIb3DQEBA...
      # RSA/SM2私钥（非对称加密使用）
      private-key: MIICdQIBADANBgkqhki...
      # mybatis需要加密的字段名称（查询条件）
      mybatis-crypto-column:
        - id_card
        - mobile
        - password
```

### 启用加密功能

在启动类或配置类上添加 `@EnableParamsCrypto` 注解：

```java
/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description 应用启动类
 */
@EnableParamsCrypto
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 接口参数加解密

1. 使用 `@ParamsCrypto` 注解标记需要加解密处理的接口：

```java
/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description 用户控制器
 */
@RestController
@RequestMapping("/users")
public class UserController {

    /**
     * 请求解密，响应加密
     */
    @PostMapping("/login")
    @ParamsCrypto
    public UserInfoVO login(@RequestBody LoginRequest request) {
        // 此时request中的敏感字段（如密码）已被自动解密
        // 返回的UserInfoVO会被自动加密
        return userService.login(request);
    }
    
    /**
     * 仅请求解密，响应不加密
     */
    @PostMapping("/register")
    @ParamsCrypto(responseEncrypt = false)
    public Result register(@RequestBody UserRegisterRequest request) {
        // 此时request中的敏感字段已被自动解密
        // 返回的Result不会被加密
        return userService.register(request);
    }
    
    /**
     * 仅响应加密，请求不解密
     */
    @GetMapping("/{id}")
    @ParamsCrypto(requestDecrypt = false)
    public UserDetailVO getUserById(@PathVariable Long id) {
        // 返回的UserDetailVO会被自动加密
        return userService.getUserById(id);
    }
}
```

2. 前端加密请求示例（JavaScript）：

```javascript
// 使用AES加密（示例）
function encrypt(data, key) {
  const cipher = CryptoJS.AES.encrypt(JSON.stringify(data), key);
  return cipher.toString();
}

// 发送加密请求
async function login(username, password) {
  // 原始数据
  const data = { username, password };
  
  // 加密数据
  const encryptedData = encrypt(data, 'Ab123456AbcDefGh');
  
  // 发送请求
  const response = await fetch('/users/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(encryptedData)
  });
  
  // 解密响应
  const encryptedResult = await response.json();
  return decrypt(encryptedResult, 'Ab123456AbcDefGh');
}
```

### 实体类字段加解密

1. 在实体类字段上添加 `@CryptoField` 注解：

```java
/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description 用户实体类
 */
@Data
public class User {
    private Long id;
    private String username;
    
    // 加密存储，解密读取
    @CryptoField
    private String mobile;
    
    // 加密存储，解密读取
    @CryptoField
    private String idCard;
    
    // 仅加密存储，不解密读取
    @CryptoField(decrypt = false)
    private String password;
    
    private String email;
    private Integer status;
}
```

2. MyBatis 集成会自动处理标记的字段：

```java
/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description 用户Mapper
 */
@Mapper
public interface UserMapper {
    
    // 插入时，带@CryptoField注解的字段会自动加密
    int insert(User user);
    
    // 查询时，带@CryptoField注解的字段会自动解密
    User selectById(Long id);
    
    // 带@CryptoField注解的字段会自动解密
    List<User> selectByIdCard(@Param("idCard") String idCard);
    
    // 更新时，带@CryptoField注解的字段会自动加密
    int updateById(User user);
}
```

## 加密算法详解

### AES（对称加密）

Advanced Encryption Standard，高级加密标准，是一种对称加密算法，加解密使用相同的密钥。

**特点**：
- 加解密速度快
- 适合大量数据加密
- 密钥长度可以是128/192/256位（16/24/32字节）

**配置示例**：
```yaml
common.params.crypto:
  type: AES
  key: Ab123456AbcDefGh  # 16字节对称密钥
```

### RSA（非对称加密）

RSA是基于大数因子分解难题的非对称加密算法，使用公钥加密、私钥解密。

**特点**：
- 安全性高
- 加解密速度相对较慢
- 适合少量数据加密或数字签名

**配置示例**：
```yaml
common.params.crypto:
  type: RSA
  public-key: MIGfMA0GCSqGSIb3DQEBA...  # 公钥
  private-key: MIICdQIBADANBgkqhki...   # 私钥
```

### SM2（国密非对称加密）

SM2是中国国家密码局发布的非对称加密算法，基于椭圆曲线密码学。

**特点**：
- 国密算法，安全性高
- 密钥长度短，性能优于RSA
- 适合对安全等级要求高的场景

**配置示例**：
```yaml
common.params.crypto:
  type: SM2
  public-key: 04A0A91D177FC792...  # SM2公钥
  private-key: 6D5233F675EEB18D...  # SM2私钥
```

### SM4（国密对称加密）

SM4是中国国家密码局发布的分组密码算法，类似于AES。

**特点**：
- 国密算法，安全性高
- 固定128位（16字节）密钥长度
- 加解密速度快

**配置示例**：
```yaml
common.params.crypto:
  type: SM4
  key: Ab123456AbcDefGh  # 16字节对称密钥
```

## 进阶功能

### 生成加密密钥

通过注入 `ParamsCryptoProvider` 来生成各类加密密钥：

```java
/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description 密钥生成服务
 */
@Service
public class KeyGenerationService {

    private final ParamsCryptoProvider cryptoProvider;
    
    public KeyGenerationService(ParamsCryptoProvider cryptoProvider) {
        this.cryptoProvider = cryptoProvider;
    }
    
    /**
     * 生成AES密钥
     */
    public String generateAESKey() {
        // 生成16字节(128位)的AES密钥
        return cryptoProvider.generateKey(16);
    }
    
    /**
     * 生成RSA密钥对
     */
    public ParamsCryptoProvider.RSAKeyPair generateRSAKeyPair() {
        // 生成2048位的RSA密钥对
        return cryptoProvider.generateRSAKeyPair(2048);
    }
}
```

### 手动加解密

直接使用 `ParamsCryptoProvider` 进行手动加解密：

```java
/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description 手动加解密服务
 */
@Service
public class ManualCryptoService {

    private final ParamsCryptoProvider cryptoProvider;
    
    public ManualCryptoService(ParamsCryptoProvider cryptoProvider) {
        this.cryptoProvider = cryptoProvider;
    }
    
    /**
     * 手动加密字符串
     */
    public String encrypt(String plainText) {
        return cryptoProvider.encrypt(plainText);
    }
    
    /**
     * 手动解密字符串
     */
    public String decrypt(String encryptedText) {
        return cryptoProvider.decrypt(encryptedText);
    }
    
    /**
     * 动态更新密钥
     */
    public void updateKey(String newKey) {
        cryptoProvider.refreshKey(newKey);
    }
}
```

### 配置热更新

支持通过 Spring Cloud Config 或手动调用方式动态更新密钥：

```java
/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description 密钥管理服务
 */
@Service
public class KeyManagementService {

    private final ParamsCryptoProperties properties;
    private final ParamsCryptoProvider cryptoProvider;
    
    public KeyManagementService(ParamsCryptoProperties properties, 
                              ParamsCryptoProvider cryptoProvider) {
        this.properties = properties;
        this.cryptoProvider = cryptoProvider;
    }
    
    /**
     * 刷新对称加密密钥
     */
    public void refreshSymmetricKey(String newKey) {
        properties.setKey(newKey);
        cryptoProvider.refreshKey(newKey);
    }
    
    /**
     * 刷新RSA/SM2密钥对
     */
    public void refreshAsymmetricKeys(String publicKey, String privateKey) {
        properties.setPublicKey(publicKey);
        properties.setPrivateKey(privateKey);
        cryptoProvider.refreshRSAKey(publicKey, privateKey);
    }
}
```

## MyBatis 字段加密详解

### 1. 实体类字段加密

使用 `@CryptoField` 注解标记需要自动加解密的字段：

```java
/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description 支付信息实体类
 */
@Data
public class PaymentInfo {
    private Long id;
    
    @CryptoField
    private String cardNumber;  // 银行卡号加解密
    
    @CryptoField
    private String cvv;  // 安全码加解密
    
    private String expiryDate;  // 不加密
}
```

### 2. 查询条件加密

配置需要自动加密的查询参数：

```yaml
common:
  params:
    crypto:
      mybatis-crypto-column:
        - card_number  # 数据库字段名
        - cvv          # 数据库字段名
```

在 Mapper 中的查询会自动处理参数加密：

```java
/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description 支付Mapper
 */
@Mapper
public interface PaymentMapper {
    
    // 查询条件中的card_number会自动加密后再查询
    List<PaymentInfo> findByCardNumber(@Param("cardNumber") String cardNumber);
    
    // 支持各种复杂条件查询
    List<PaymentInfo> findByCondition(Map<String, Object> params);
}
```

### 3. 结果集解密

执行查询后，结果集中带有 `@CryptoField` 注解的字段会自动解密：

```java
/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description 支付服务
 */
@Service
public class PaymentService {
    
    private final PaymentMapper paymentMapper;
    
    public PaymentService(PaymentMapper paymentMapper) {
        this.paymentMapper = paymentMapper;
    }
    
    public List<PaymentInfo> findByCardNumber(String cardNumber) {
        // 查询参数cardNumber会自动加密
        // 返回的PaymentInfo中的加密字段会自动解密
        return paymentMapper.findByCardNumber(cardNumber);
    }
}
```

## 最佳实践

1. **密钥管理**：
   - 生产环境不要使用默认密钥
   - 通过安全的配置中心管理密钥
   - 定期更换密钥

2. **算法选择**：
   - 大量数据：优先使用AES或SM4等对称加密
   - 高安全场景：考虑RSA或SM2等非对称加密
   - 国内应用：推荐使用SM2/SM4等国密算法

3. **性能优化**：
   - 避免频繁加解密大量数据
   - 考虑使用缓存机制减少重复加解密
   - 对于不经常变更的敏感数据，考虑使用双向加密存储

4. **安全防护**：
   - 防止密钥硬编码到代码中
   - 适当的日志记录（不记录敏感信息和密钥）
   - 与HTTPS等传输加密结合使用

## 应用场景

- **API安全**：保护敏感API的请求和响应数据
- **隐私保护**：加密存储用户隐私数据（身份证、手机号等）
- **支付安全**：保护支付相关信息（银行卡号、CVV等）
- **合规要求**：满足数据安全和隐私保护的法规要求
- **防篡改**：防止数据在传输过程中被篡改

## 贡献 🙏

欢迎提交 Issue 或 Pull Request 参与项目贡献！

## 许可证

本项目使用 [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0.html) 许可证。 