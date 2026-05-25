# Signature Spring Boot Starter

API 签名验证组件，支持防篡改、防重放攻击。

## 特性

- **防篡改**：基于 HMAC/SHA 算法对请求参数签名，防止数据被篡改
- **防重放**：基于 Nonce + Timestamp 机制，防止请求被重放攻击
- **多种算法**：支持 MD5、SHA1、SHA256、SHA512、HMAC-SHA256、HMAC-SHA512
- **注解驱动**：支持 `@SignatureVerify` 和 `@IgnoreSignature` 注解
- **灵活配置**：支持请求头或请求参数传递签名信息
- **JSON Body 解析**：支持将 JSON 请求体字段解析后参与签名排序
- **存储接口**：NonceStore 和 AppSecretStore 提供接口，由用户实现具体存储
- **内置实现**：提供基于内存的默认实现（适合单机部署）

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>io.github.archer099</groupId>
    <artifactId>signature-spring-boot-starter</artifactId>
    <version>${version}</version>
</dependency>
```

### 2. 配置

```yaml
signature:
  enabled: true
  # 签名算法
  algorithm: HMAC_SHA256
  # 时间戳有效期（5分钟）
  timestamp-tolerance: 5m
  # Nonce 有效期（10分钟）
  nonce-ttl: 10m
  # 是否启用时间戳验证
  timestamp-enabled: true
  # 是否启用 Nonce 验证（防重放）
  nonce-enabled: true
  # 签名参数配置
  app-key-header: X-App-Key
  signature-header: X-Signature
  timestamp-header: X-Timestamp
  nonce-header: X-Nonce
  # 排除路径
  exclude-paths:
    - /api/public/**
    - /health
  # 简单模式：配置 AppKey 和 AppSecret
  apps:
    myapp: my-secret-key-123456
    testapp: test-secret-key-654321
  # 开启调试模式
  debug: true
```

### 3. 使用注解

```java
// 类级别启用签名验证
@RestController
@RequestMapping("/api")
@SignatureVerify
public class ApiController {
    
    // 需要签名验证（继承类注解）
    @GetMapping("/data")
    public Result getData() { }
    
    // 验证请求体（原始字符串方式）
    @PostMapping("/submit")
    @SignatureVerify(verifyBody = true)
    public Result submit(@RequestBody Data data) { }
    
    // 验证请求体（JSON 字段解析方式，推荐）
    @PostMapping("/order")
    @SignatureVerify(verifyBody = true, parseJsonBody = true)
    public Result createOrder(@RequestBody OrderDTO order) { }
    
    // 忽略签名验证
    @GetMapping("/public")
    @IgnoreSignature
    public Result publicApi() { }
}
```

## 签名算法详解

### 签名参数

客户端需要传递以下参数（通过请求头或 URL 参数）：

| 参数 | 说明 | 示例 |
|------|------|------|
| appKey | 应用标识 | myapp |
| timestamp | 时间戳（秒） | 1705555555 |
| nonce | 随机字符串（32位） | abc123def456... |
| signature | 签名值 | e5b7c3d8f2a1... |

### 签名计算步骤

#### 步骤1：准备参数

将所有参与签名的参数放入 Map：

```java
Map<String, String> params = new TreeMap<>();
params.put("appKey", "myapp");
params.put("timestamp", "1705555555");
params.put("nonce", "abc123def456789012345678901234");
// 添加其他业务参数
params.put("userId", "123");
params.put("amount", "100.00");
```

#### 步骤2：参数排序拼接

按 key 字典序排序，拼接成 `key1=value1&key2=value2` 格式：

```
appKey=myapp&amount=100.00&nonce=abc123def456789012345678901234&timestamp=1705555555&userId=123
```

#### 步骤3：拼接密钥

在末尾拼接 `&secret=AppSecret`：

```
appKey=myapp&amount=100.00&nonce=abc123def456789012345678901234&timestamp=1705555555&userId=123&secret=my-secret-key-123456
```

#### 步骤4：计算签名

使用配置的算法（默认 HMAC-SHA256）计算签名：

```java
String signature = HmacSHA256(signContent, appSecret);
// 结果: e5b7c3d8f2a1b4c5d6e7f8...
```

### 完整示例（Java）

```java
public class SignatureClient {
    
    private static final String APP_KEY = "myapp";
    private static final String APP_SECRET = "my-secret-key-123456";
    
    public static void main(String[] args) {
        // 1. 准备参数
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String nonce = UUID.randomUUID().toString().replace("-", "");
        
        Map<String, String> params = new TreeMap<>();
        params.put("appKey", APP_KEY);
        params.put("timestamp", timestamp);
        params.put("nonce", nonce);
        params.put("userId", "123");
        params.put("amount", "100.00");
        
        // 2. 生成签名
        String signature = generateSignature(params, APP_SECRET);
        
        // 3. 发送请求
        HttpRequest.get("http://api.example.com/api/data")
            .header("X-App-Key", APP_KEY)
            .header("X-Signature", signature)
            .header("X-Timestamp", timestamp)
            .header("X-Nonce", nonce)
            .form("userId", "123")
            .form("amount", "100.00")
            .execute();
    }
    
    /**
     * 生成签名
     */
    public static String generateSignature(Map<String, String> params, String secret) {
        // 排序并拼接参数
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : new TreeMap<>(params).entrySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(entry.getKey()).append("=").append(entry.getValue());
        }
        
        // 拼接密钥
        String signContent = sb + "&secret=" + secret;
        
        // 计算 HMAC-SHA256
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(), "HmacSHA256"));
        byte[] hash = mac.doFinal(signContent.getBytes());
        
        // 转换为16进制
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
```

### 完整示例（JavaScript）

```javascript
const crypto = require('crypto');

const APP_KEY = 'myapp';
const APP_SECRET = 'my-secret-key-123456';

// 生成签名
function generateSignature(params, secret) {
    // 排序参数
    const sortedKeys = Object.keys(params).sort();
    
    // 拼接参数
    const signContent = sortedKeys
        .map(key => `${key}=${params[key]}`)
        .join('&') + `&secret=${secret}`;
    
    // 计算 HMAC-SHA256
    return crypto
        .createHmac('sha256', secret)
        .update(signContent)
        .digest('hex');
}

// 发送请求
async function callApi() {
    const timestamp = Math.floor(Date.now() / 1000).toString();
    const nonce = crypto.randomUUID().replace(/-/g, '');
    
    const params = {
        appKey: APP_KEY,
        timestamp: timestamp,
        nonce: nonce,
        userId: '123',
        amount: '100.00'
    };
    
    const signature = generateSignature(params, APP_SECRET);
    
    const response = await fetch('http://api.example.com/api/data?userId=123&amount=100.00', {
        headers: {
            'X-App-Key': APP_KEY,
            'X-Signature': signature,
            'X-Timestamp': timestamp,
            'X-Nonce': nonce
        }
    });
    
    return response.json();
}
```

## 验证 POST 请求体

### 方式1：原始字符串方式

请求体作为整体参与签名：

```java
@SignatureVerify(verifyBody = true)
public Result submit(@RequestBody String body) { }
```

签名内容：
```
appKey=myapp&nonce=xxx&timestamp=xxx&body={"userId":"123","amount":"100"}&secret=xxx
```

### 方式2：JSON 字段解析方式（推荐）

JSON 字段解析后参与排序签名，每个字段都参与验证：

```java
@SignatureVerify(verifyBody = true, parseJsonBody = true)
public Result submit(@RequestBody OrderDTO order) { }
```

假设请求体：
```json
{
    "orderId": "ORD001",
    "userId": "123",
    "amount": 100.00
}
```

签名内容（字段带 `body.` 前缀）：
```
appKey=myapp&body.amount=100.0&body.orderId=ORD001&body.userId=123&nonce=xxx&timestamp=xxx&secret=xxx
```

### 嵌套 JSON 处理

嵌套对象会递归展开：

```json
{
    "orderId": "ORD001",
    "user": {
        "userId": "123",
        "username": "张三"
    },
    "items": [{"name": "商品A"}]
}
```

签名参数：
```
body.orderId=ORD001
body.user.userId=123
body.user.username=张三
body.items=[{"name":"商品A"}]    // 数组转为 JSON 字符串
```

### Java 客户端示例（带 Body）

```java
public static void submitOrder(OrderDTO order) {
    String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
    String nonce = UUID.randomUUID().toString().replace("-", "");
    
    // 基础参数
    Map<String, String> params = new TreeMap<>();
    params.put("appKey", APP_KEY);
    params.put("timestamp", timestamp);
    params.put("nonce", nonce);
    
    // 将 JSON 字段加入签名参数
    String jsonBody = JSON.toJSONString(order);
    JSONObject json = JSON.parseObject(jsonBody);
    flattenJson(json, "body.", params);
    
    // 生成签名
    String signature = generateSignature(params, APP_SECRET);
    
    // 发送请求
    HttpRequest.post("http://api.example.com/api/order")
        .header("X-App-Key", APP_KEY)
        .header("X-Signature", signature)
        .header("X-Timestamp", timestamp)
        .header("X-Nonce", nonce)
        .header("Content-Type", "application/json")
        .body(jsonBody)
        .execute();
}

/**
 * 将 JSON 对象扁平化
 */
private static void flattenJson(JSONObject json, String prefix, Map<String, String> result) {
    for (String key : json.keySet()) {
        Object value = json.get(key);
        String fullKey = prefix + key;
        
        if (value instanceof JSONObject) {
            flattenJson((JSONObject) value, fullKey + ".", result);
        } else if (value instanceof JSONArray) {
            result.put(fullKey, value.toString());
        } else if (value != null) {
            result.put(fullKey, value.toString());
        }
    }
}
```

## 自定义存储

### 实现 NonceStore（Redis 示例）

```java
@Component
public class RedisNonceStore implements NonceStore {
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    private static final String PREFIX = "signature:nonce:";
    
    @Override
    public boolean exists(String nonce) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(PREFIX + nonce));
    }
    
    @Override
    public boolean store(String nonce, Duration ttl) {
        redisTemplate.opsForValue().set(PREFIX + nonce, "1", ttl);
        return true;
    }
    
    @Override
    public boolean storeIfAbsent(String nonce, Duration ttl) {
        Boolean result = redisTemplate.opsForValue()
                .setIfAbsent(PREFIX + nonce, "1", ttl);
        return Boolean.TRUE.equals(result);
    }
    
    @Override
    public void remove(String nonce) {
        redisTemplate.delete(PREFIX + nonce);
    }
}
```

### 实现 AppSecretStore（数据库示例）

```java
@Component
public class DatabaseAppSecretStore implements AppSecretStore {
    
    @Autowired
    private AppRepository appRepository;
    
    @Override
    public String getSecret(String appKey) {
        App app = appRepository.findByAppKey(appKey);
        return app != null ? app.getAppSecret() : null;
    }
    
    @Override
    public boolean isValid(String appKey) {
        App app = appRepository.findByAppKey(appKey);
        return app != null && app.isEnabled();
    }
    
    @Override
    public String getAppName(String appKey) {
        App app = appRepository.findByAppKey(appKey);
        return app != null ? app.getAppName() : appKey;
    }
}
```

## 配置说明

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `signature.enabled` | boolean | true | 是否启用签名验证 |
| `signature.algorithm` | enum | HMAC_SHA256 | 签名算法 |
| `signature.timestamp-tolerance` | Duration | 5m | 时间戳有效期 |
| `signature.nonce-ttl` | Duration | 10m | Nonce 有效期 |
| `signature.timestamp-enabled` | boolean | true | 是否验证时间戳 |
| `signature.nonce-enabled` | boolean | true | 是否验证 Nonce |
| `signature.app-key-header` | String | X-App-Key | AppKey 请求头名 |
| `signature.signature-header` | String | X-Signature | 签名请求头名 |
| `signature.timestamp-header` | String | X-Timestamp | 时间戳请求头名 |
| `signature.nonce-header` | String | X-Nonce | Nonce 请求头名 |
| `signature.allow-query-params` | boolean | true | 是否允许从请求参数获取签名信息 |
| `signature.exclude-paths` | List | 空 | 排除路径 |
| `signature.include-paths` | List | 空 | 包含路径（为空时验证所有） |
| `signature.apps` | Map | 空 | AppKey-Secret 映射 |
| `signature.parse-json-body` | boolean | false | 全局 JSON Body 解析开关 |
| `signature.json-body-prefix` | String | body. | JSON 字段前缀 |
| `signature.debug` | boolean | false | 调试模式 |

## 注解说明

### @SignatureVerify

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `enabled` | boolean | true | 是否启用签名验证 |
| `verifyBody` | boolean | false | 是否验证请求体 |
| `parseJsonBody` | boolean | false | 是否解析 JSON Body 字段 |
| `verifyTimestamp` | boolean | true | 是否验证时间戳 |
| `verifyNonce` | boolean | true | 是否验证 Nonce |

### @IgnoreSignature

标记方法或类忽略签名验证。

## 错误码

| 错误码 | 说明 |
|--------|------|
| APPKEY_MISSING | AppKey 参数缺失 |
| APPKEY_INVALID | AppKey 无效 |
| SIGNATURE_MISSING | 签名参数缺失 |
| SIGNATURE_INVALID | 签名验证失败 |
| TIMESTAMP_MISSING | 时间戳参数缺失 |
| TIMESTAMP_INVALID | 时间戳格式无效 |
| TIMESTAMP_EXPIRED | 请求已过期 |
| NONCE_MISSING | Nonce 参数缺失 |
| NONCE_REPLAY | 请求重复提交 |

## 调试指南

开启调试模式后，日志会输出详细的签名计算过程：

```yaml
signature:
  debug: true
```

日志示例：
```
========== 签名验证调试信息 ==========
parseJsonBody模式: true
原始参数: {appKey=myapp, nonce=xxx, timestamp=xxx}
请求体(body): {"userId":"123","amount":"100"}
加密前完整字符串: appKey=myapp&body.amount=100&body.userId=123&nonce=xxx&timestamp=xxx&secret=my-secret-key
服务端计算签名: abc123...
客户端传递签名: abc123...
签名是否匹配: true
======================================
```

## 注意事项

1. **分布式部署**：需要实现基于 Redis 的 NonceStore，内存实现仅适合单机
2. **时钟同步**：客户端与服务端时钟需要同步，建议使用 NTP
3. **HTTPS**：签名不能替代 HTTPS，建议同时使用
4. **密钥安全**：AppSecret 应妥善保管，不要在客户端暴露
5. **JSON 顺序**：使用 `parseJsonBody=true` 时，JSON 字段顺序不影响签名结果

## License

Apache License 2.0
