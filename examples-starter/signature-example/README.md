# Signature Example

API 签名验证组件 (signature-spring-boot-starter) 示例项目。

## 功能演示

- 签名生成
- 签名验证
- 安全接口调用（需要签名）
- 公开接口调用（无需签名）
- 防重放测试

## 快速启动

```bash
# 在项目根目录执行
mvn clean install -DskipTests

# 启动示例
cd examples-starter/signature-example
mvn spring-boot:run
```

访问 http://localhost:8081 查看演示界面。

## 预置应用密钥

| AppKey | AppSecret |
|--------|-----------|
| myapp | my-secret-key-123456 |
| testapp | test-secret-key-654321 |
| demo | demo-secret-key-abcdef |

## API 接口

### 安全接口（需要签名）

```bash
# GET 请求
curl -X GET 'http://localhost:8081/api/secure/data?param1=hello&param2=world' \
  -H 'X-App-Key: myapp' \
  -H 'X-Signature: <签名>' \
  -H 'X-Timestamp: <时间戳>' \
  -H 'X-Nonce: <随机数>'

# POST 请求
curl -X POST 'http://localhost:8081/api/secure/submit' \
  -H 'Content-Type: application/json' \
  -H 'X-App-Key: myapp' \
  -H 'X-Signature: <签名>' \
  -H 'X-Timestamp: <时间戳>' \
  -H 'X-Nonce: <随机数>' \
  -d '{"userId":"123","action":"test","data":"hello"}'
```

### 公开接口（无需签名）

```bash
curl http://localhost:8081/api/public/info
```

### 工具接口

```bash
# 生成签名
curl -X POST 'http://localhost:8081/api/tool/sign' \
  -H 'Content-Type: application/json' \
  -d '{
    "appKey": "myapp",
    "appSecret": "my-secret-key-123456",
    "param1": "hello",
    "param2": "world"
  }'

# 验证签名
curl -X POST 'http://localhost:8081/api/tool/verify' \
  -H 'Content-Type: application/json' \
  -d '{
    "appKey": "myapp",
    "signature": "<签名>",
    "timestamp": "<时间戳>",
    "nonce": "<随机数>",
    "param1": "hello",
    "param2": "world"
  }'
```

## 签名流程

1. 将所有请求参数按 key 字典序排序
2. 拼接成 `key1=value1&key2=value2` 格式
3. 末尾拼接 `&secret=AppSecret`
4. 使用 HMAC-SHA256 算法计算签名

### 示例

```
原始参数：
  appKey=myapp
  timestamp=1705555555
  nonce=abc123def456
  param1=hello
  param2=world

排序拼接：
  appKey=myapp&nonce=abc123def456&param1=hello&param2=world&timestamp=1705555555

拼接密钥：
  appKey=myapp&nonce=abc123def456&param1=hello&param2=world&timestamp=1705555555&secret=my-secret-key-123456

计算签名（HMAC-SHA256）：
  e5b7c3d8f2a1...
```

## 防重放测试

1. 使用相同的 nonce 发送两次请求
2. 第二次请求会返回 `NONCE_REPLAY` 错误

## 时间戳过期测试

1. 使用过期的时间戳（超过 5 分钟）发送请求
2. 请求会返回 `TIMESTAMP_EXPIRED` 错误

## 注意事项

1. 时间戳单位为秒（Unix timestamp）
2. Nonce 必须唯一，建议使用 UUID
3. 签名大小写不敏感
4. 调试模式会输出签名计算过程（生产环境请关闭）
