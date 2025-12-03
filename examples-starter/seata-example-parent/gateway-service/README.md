# Gateway Service (网关服务)

本模块是基于 **Spring Cloud Gateway** 构建的 API 网关，作为整个微服务系统的统一入口。集成了 **Nacos** 服务发现、**Spring Cloud LoadBalancer** 负载均衡，并实现了 **灰度发布** 和 **自动重试** 机制。

## 1. 核心功能

### 1.1 服务路由
集成 Nacos 注册中心，自动发现并转发请求到后端服务：
- `/business/**` -> `business-service`
- `/order/**` -> `order-service`
- `/storage-api/**` -> `storage-service` (路径重写为 `/storage/**`)
- `/account/**` -> `account-service`

### 1.2 灰度发布 (Gray Release)
自定义负载均衡器 `GrayRoundRobinLoadBalancer`，支持基于请求头 `version` 的流量路由。

*   **原理**: 优先匹配 Nacos 实例元数据中 `version` 与请求头 `version` 一致的实例。
*   **场景**: 验证新版本功能时，通过特定 Header 将流量引导至新版实例。

### 1.3 自动重试 (Retry)
全局配置了重试过滤器，增强系统可用性。
*   **触发条件**: `5xx` 错误 (BAD_GATEWAY, SERVICE_UNAVAILABLE, GATEWAY_TIMEOUT, INTERNAL_SERVER_ERROR)。
*   **重试策略**: 最多重试 3 次，指数退避 (50ms -> 100ms -> ...)。

### 1.4 服务限流 (Rate Limiting)
针对 `business-service` 实现了基于 **Bucket4j** + **Caffeine** (本地缓存) 的限流。
*   **算法**: 令牌桶算法 (Token Bucket)。
*   **规则**: 容量 10，每秒补充 1 个令牌 (即 QPS ≈ 1)。
*   **限流 Key**: 优先使用请求参数 `userId`，若无则使用客户端 IP。

### 1.5 路径重写 (RewritePath)
针对 `storage-service` 配置了路径重写功能。
*   **规则**: `/storage-api/**` -> `/storage/**`。
*   **场景**: 对外暴露的 API 路径与内部服务实际路径不一致时，隐藏内部实现细节。

### 1.6 超时配置 (Timeouts)
配置了全局的 HTTP 超时时间，防止请求长时间挂起。
*   **连接超时 (Connect Timeout)**: 2000ms
*   **响应超时 (Response Timeout)**: 5s

### 1.7 动态路由 (Dynamic Routing)
集成了 **Redis** 存储路由定义，支持通过 Actuator 接口动态添加、删除路由，且支持多网关实例共享。
*   **存储**: Redis (`spring.cloud.gateway.redis-route-definition-repository.enabled=true`)
*   **管理接口**: `/actuator/gateway/routes/{id}`
*   **可视化管理**: 提供静态页面 `http://localhost:8080/route-manager.html` 进行路由的 CURD 操作。

## 2. 配置说明

核心配置位于 `application.yml`：

```yaml
spring:
  cloud:
    gateway:
      httpclient:
        connect-timeout: 2000
        response-timeout: 5s
      discovery:
        locator:
          enabled: true # 开启服务发现路由
      default-filters:
        - name: Retry
          args:
            retries: 3
            statuses: BAD_GATEWAY,SERVICE_UNAVAILABLE,...
```

## 3. 使用指南

### 3.1 启动
运行 `GatewayApplication`，服务端口默认为 `8080`。

### 3.2 普通请求
直接访问网关端口，网关会轮询转发到后端服务。

```bash
curl -X POST "http://localhost:8080/business/purchase?userId=user-1&commodityCode=product-1&count=2"
```

### 3.3 灰度路由测试

1.  **设置实例元数据**: 在 Nacos 控制台，给某个服务实例添加元数据 `version` = `gray`。
2.  **发送灰度请求**:

```bash
curl -X POST -H "version: gray" "http://localhost:8080/business/purchase?userId=user-1&commodityCode=product-1&count=2"
```

*   **效果**: 请求将被优先转发到标记为 `gray` 的实例。

### 3.4 重试测试
停止后端服务（如 `storage-service`），通过网关请求，观察网关日志，可以看到重试过程。

### 3.5 限流测试
`business-service` 配置了 QPS ≈ 1 的限流规则。
快速连续发送请求（超过 10 次突发或 1 次/秒持续）：

```bash
for /l %i in (1,1,20) do curl -X POST "http://localhost:8080/business/purchase?userId=user-1&commodityCode=product-1&count=2"
```

*   **效果**: 部分请求返回 `HTTP 429 Too Many Requests`。

### 3.6 路径重写测试
`storage-service` 的接口路径为 `/storage/deduct`，但在网关配置了 `/storage-api/**` 的路由。

```bash
# 访问 /storage-api/deduct，网关会自动重写为 /storage/deduct
curl -X POST "http://localhost:8080/storage-api/deduct?commodityCode=product-1&count=1"
```

*   **效果**: 请求成功转发到库存服务，返回 `200 OK`。

### 3.7 动态路由测试
通过 Actuator 接口动态添加一个路由。

1.  **添加路由**:
```bash
curl -X POST "http://localhost:8080/actuator/gateway/routes/dynamic-route" \
-H "Content-Type: application/json" \
-d '{
  "id": "dynamic-route",
  "uri": "https://www.baidu.com",
  "predicates": [{"name": "Path", "args": {"pattern": "/baidu/**"}}],
  "filters": [{"name": "StripPrefix", "args": {"parts": "1"}}]
}'
```

2.  **访问测试**:
```bash
# 访问 /baidu，将被转发到 www.baidu.com
curl -L http://localhost:8080/baidu
```

也可以直接访问 [http://localhost:8080/route-manager.html](http://localhost:8080/route-manager.html) 使用可视化界面管理路由。
