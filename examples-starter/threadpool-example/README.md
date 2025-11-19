# 线程池示例应用

这是一个完整的线程池监控和测试示例应用。

## 功能特性

- ✅ 线程池动态配置
- ✅ TTL 上下文传递演示
- ✅ 实时监控 Dashboard
- ✅ 任务提交和压力测试
- ✅ 告警配置示例

## 快速开始

### 1. 启动应用

```bash
cd examples-starter/threadpool-example
mvn spring-boot:run
```

### 2. 访问页面

打开浏览器访问: http://localhost:8080

### 3. 测试功能

#### 提交任务
在页面上选择线程池，设置任务数量和耗时，点击"提交任务"

####  压力测试
点击"压力测试"按钮，会提交200个长时间任务，观察告警触发

#### 查看指标
页面每3秒自动刷新指标，可实时查看：
- 活跃线程数
- 队列使用率
- 完成任务数
- 拒绝次数

## API 端点

### REST API

- `POST /api/threadpool/submit` - 提交任务
- `GET /api/threadpool/metrics` - 获取所有线程池指标
- `GET /api/threadpool/metrics/{poolName}` - 获取指定线程池指标
- `GET /api/threadpool/pools` - 获取所有线程池列表
- `POST /api/threadpool/stress` - 压力测试

### Actuator 端点

- `GET /actuator/threadpool` - 线程池监控端点
- `POST /actuator/threadpool/{poolName}` - 更新线程池配置
- `GET /actuator/health` - 健康检查
- `GET /actuator/prometheus` - Prometheus metrics

## 配置说明

### 线程池配置

```yaml
dynamic-threadpool:
  global:
    enableTtl: true  # 启用 TTL
  pools:
    order-pool:
      corePoolSize: 10
      maxPoolSize: 20
```

### 告警配置

```yaml
dynamic-threadpool:
  alarm:
    channelType: WECHAT  # 可选: LOG, WECHAT, DINGTALK, ALL
    wechatWebhook: https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=YOUR_KEY
```

## TTL 演示

应用使用 TransmittableThreadLocal 演示上下文传递。每个请求会生成一个 RequestId，并在线程池中自动传递。

查看日志可以看到：
```
Task-1 started in pool [demo-pool], Context: REQ-1234567890, Thread: demo-1
```

## 监控演示

访问 http://localhost:8080/actuator/prometheus 可以看到 Prometheus 格式的指标导出。
