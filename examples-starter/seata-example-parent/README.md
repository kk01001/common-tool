# Seata 分布式事务示例工程

本工程基于 Spring Cloud Alibaba + Seata + Nacos + MyBatis-Plus 构建，演示了 Seata AT 模式下的分布式事务处理。

## 1. 工程目录结构

```
seata-example-parent
├── seata-common       # 公共模块 (Result, 异常定义等)
├── account-service    # 账户服务 (8083) - 扣减余额
├── order-service      # 订单服务 (8082) - 创建订单
├── storage-service    # 库存服务 (8081) - 扣减库存
├── business-service   # 业务聚合服务 (8084) - 全局事务入口
└── gateway-service    # 网关服务 (8080) - 统一入口 & 灰度发布 & 重试机制

## 2. 架构流程

模拟电商下单场景：
1. 用户请求 **Gateway Service** (可选) 或直接请求 **Business Service**。
2. **Business Service** 开启全局事务 (`@GlobalTransactional`)。
3. 调用 **Storage Service** 扣减库存。
4. 调用 **Order Service** 创建订单。
5. **Order Service** 内部调用 **Account Service** 扣减用户余额。
6. 如果任一环节失败，Seata 协调所有服务回滚；如果成功，提交事务。

## 3. 环境准备

### 3.1 中间件
- **Nacos Server**: 启动 Nacos，地址默认 `127.0.0.1:8848`。
- **Seata Server**: 启动 Seata Server (版本建议 2.x)，配置注册中心和配置中心为 Nacos。
- **MySQL**: 准备 MySQL 数据库。

### 3.2 数据库初始化
请在 MySQL 中执行 `seata_demo.sql` 脚本。
脚本已按模块划分，请按以下说明操作：

1.  **Seata Server 数据库 (通常命名为 `seata`)**:
    *   包含表: `global_table`, `branch_table`, `lock_table`, `distributed_lock`。
    *   这些表供 Seata TC (Transaction Coordinator) 服务端使用 (仅当 store.mode=db 时)。

2.  **业务数据库 (客户端)**:
    *   **`seata_storage`**: 包含 `storage_tbl` 和 `undo_log`。
    *   **`seata_order`**: 包含 `order_tbl` 和 `undo_log`。
    *   **`seata_account`**: 包含 `account_tbl` 和 `undo_log`。
    *   **注意**: AT 模式下，**每个** 参与分布式事务的业务数据库都 **必须** 包含 `undo_log` 表，用于记录回滚日志。

### 3.3 服务启动
依次启动以下服务：
1. `StorageApplication`
2. `AccountApplication`
3. `OrderApplication`
4. `BusinessApplication`
5. `GatewayApplication`

## 4. 测试方法

### 4.1 正常下单测试
请求业务服务接口，模拟用户 `user-1` 购买商品 `product-1`，数量为 `2`。

**CURL (直接调用):**
```bash
curl -X POST "http://localhost:8084/business/purchase?userId=user-1&commodityCode=product-1&count=2"
```

**CURL (通过网关):**
```bash
curl -X POST "http://localhost:8080/business/purchase?userId=user-1&commodityCode=product-1&count=2"
```

**预期结果:**
- 接口返回 200 成功。
- `storage_tbl`: `product-1` 库存减少 2。
- `order_tbl`: 新增一条订单记录。
- `account_tbl`: `user-1` 余额减少 (假设单价为 5，总价 10)。

### 4.2 异常回滚测试
为了测试分布式事务回滚，可以在 `AccountServiceImpl` 或 `OrderServiceImpl` 中手动抛出异常（例如在扣款后抛出 `RuntimeException`）。

**步骤:**
1. 修改代码抛出异常。
2. 重启对应服务。
3. 再次执行上述 CURL 请求。

**预期结果:**
- 接口返回 500 失败。
- 检查数据库：库存、订单、余额均未发生变化（数据回滚到初始状态）。

### 4.3 灰度发布测试 (Gateway)
网关集成了自定义灰度负载均衡策略，支持根据请求头 `version` 进行流量路由。

**配置方法:**
在 Nacos 控制台中，编辑某个服务的实例详情，添加元数据 (Metadata): `version` = `gray` (或其他自定义值)。

**测试命令:**
```bash
# 路由到 version=gray 的实例
curl -X POST -H "version: gray" "http://localhost:8080/business/purchase?userId=user-1&commodityCode=product-1&count=2"
```
- 如果存在 version=gray 的实例，请求将优先转发到该实例。
- 如果不存在，或未携带 Header，则进行普通轮询。

### 4.4 网关重试机制
Gateway 配置了默认的重试过滤器 `RetryGatewayFilter`。
当后端服务返回 `5xx` 错误（如 `BAD_GATEWAY`, `SERVICE_UNAVAILABLE` 等）时，网关会自动重试请求。

**配置参数:**
- 重试次数: 3次
- 匹配状态码: `BAD_GATEWAY`, `SERVICE_UNAVAILABLE`, `GATEWAY_TIMEOUT`, `INTERNAL_SERVER_ERROR`
- 匹配方法: `GET`, `POST`, `PUT`, `DELETE`
- 退避策略: 初始 50ms，最大 500ms，指数增长 (factor=2)

**验证方法:**
1. 停止某个服务 (如 `order-service`)。
2. 通过网关发起请求。
3. 观察 Gateway 日志，会看到多次尝试请求的记录，最终返回错误。
