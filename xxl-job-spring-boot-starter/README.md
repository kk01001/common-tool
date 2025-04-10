# XXL-JOB Spring Boot Starter

[![Maven Central](https://img.shields.io/maven-central/v/io.github.kk01001/xxl-job-spring-boot-starter.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:io.github.kk01001%20a:xxl-job-spring-boot-starter)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

## 项目概述

`xxl-job-spring-boot-starter` 是基于 XXL-JOB 分布式任务调度平台的 Spring Boot Starter，提供了任务的自动注册功能。通过简单的注解和配置，可以快速将 Spring Boot 应用接入 XXL-JOB 调度中心，实现任务的自动注册和执行。

## 功能特性

- **执行器自动注册**：启动时自动向 XXL-JOB 调度中心注册当前应用为执行器
- **任务自动注册**：通过注解自动注册任务到 XXL-JOB 调度中心
- **简化配置**：提供简洁的配置方式，降低接入成本
- **完整参数支持**：支持 XXL-JOB 的所有调度参数配置
- **分片任务支持**：提供便捷的分片任务工具类

## 技术栈

- Java 21
- Spring Boot 3.x
- XXL-JOB Core
- Hutool

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>io.github.kk01001</groupId>
    <artifactId>xxl-job-spring-boot-starter</artifactId>
    <version>${latest.version}</version>
</dependency>
```

### 2. 配置属性

在 `application.yml` 中添加以下配置：

```yaml
xxl-job:
  # 是否启用 XXL-JOB
  enable: true
  # 调度中心地址，多个地址以逗号分隔
  admin-addresses: http://xxl-job-admin:8080/xxl-job-admin
  # 执行器名称，会在调度中心显示
  app-name: my-xxl-job-executor
  # 执行器标题
  title: My XXL-JOB Executor
  # 执行器端口
  port: 9999
  # 日志路径
  log-path: /data/applogs/xxl-job/jobhandler
  # 日志保留天数
  log-retention-days: 30
  # 访问令牌
  access-token: default_token
  # 调度中心登录用户名
  user-name: admin
  # 调度中心登录密码
  password: 123456
```

### 3. 创建任务

使用 `@XxlJob` 和 `@XxlJobRegister` 注解创建并注册任务：

```java
import com.xxl.job.core.handler.annotation.XxlJob;
import io.github.kk01001.xxljob.annotations.XxlJobRegister;
import io.github.kk01001.xxljob.enums.ExecutorRouteStrategyEnum;
import io.github.kk01001.xxljob.enums.MisfireStrategyEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MyXxlJobHandler {

    @XxlJob("demoJobHandler")
    @XxlJobRegister(
        cron = "0 0 1 * * ?",
        jobDesc = "示例任务",
        author = "admin",
        triggerStatus = 1,
        executorRouteStrategy = ExecutorRouteStrategyEnum.ROUND,
        misfireStrategy = MisfireStrategyEnum.DO_NOTHING
    )
    public void demoJobHandler() {
        log.info("XXL-JOB, Hello World.");
    }
}
```

## 核心注解

### @XxlJobRegister

用于自动注册 XXL-JOB 任务的注解，定义在方法上，与 `@XxlJob` 一起使用。

| 属性 | 类型 | 默认值 | 描述 |
| --- | --- | --- | --- |
| cron | String | "0 0 0 * * ? *" | Cron 表达式 |
| jobDesc | String | "xxl job desc" | 任务描述 |
| author | String | "admin" | 任务负责人 |
| triggerStatus | int | 0 | 调度状态：0-停止，1-运行 |
| executorParam | String | "" | 任务参数 |
| executorRouteStrategy | ExecutorRouteStrategyEnum | ROUND | 路由策略 |
| executorBlockStrategy | ExecutorBlockStrategyEnum | DISCARD_LATER | 阻塞处理策略 |
| misfireStrategy | MisfireStrategyEnum | DO_NOTHING | 调度过期策略 |
| executorTimeout | int | 0 | 任务执行超时时间(秒) |
| executorFailRetryCount | int | 0 | 失败重试次数 |

## 路由策略

支持的路由策略（ExecutorRouteStrategyEnum）包括：

- FIRST：第一个
- LAST：最后一个
- ROUND：轮询
- RANDOM：随机
- CONSISTENT_HASH：一致性哈希
- LEAST_FREQUENTLY_USED：最不经常使用
- LEAST_RECENTLY_USED：最近最久未使用
- FAILOVER：故障转移
- BUSYOVER：忙碌转移
- SHARDING_BROADCAST：分片广播

## 阻塞处理策略

支持的阻塞处理策略（ExecutorBlockStrategyEnum）包括：

- SERIAL_EXECUTION：单机串行
- DISCARD_LATER：丢弃后续调度
- COVER_EARLY：覆盖之前调度

## 调度过期策略

支持的调度过期策略（MisfireStrategyEnum）包括：

- DO_NOTHING：忽略
- FIRE_ONCE_NOW：立即执行一次

## 分片任务

对于需要使用分片的任务，可以使用 `XxlJobUtil` 工具类：

```java
import com.xxl.job.core.handler.annotation.XxlJob;
import io.github.kk01001.xxljob.annotations.XxlJobRegister;
import io.github.kk01001.xxljob.enums.ExecutorRouteStrategyEnum;
import io.github.kk01001.xxljob.util.XxlJobUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class ShardingJobHandler {

    @XxlJob("shardingJobHandler")
    @XxlJobRegister(
        cron = "0 0 1 * * ?",
        jobDesc = "分片任务示例",
        executorRouteStrategy = ExecutorRouteStrategyEnum.SHARDING_BROADCAST
    )
    public void shardingJobHandler() {
        // 原始数据列表
        List<String> list = Arrays.asList("A", "B", "C", "D", "E");
        
        // 获取分片后的数据
        List<String> shardList = XxlJobUtil.getShardList(list);
        
        // 处理分片数据
        for (String item : shardList) {
            log.info("开始处理分片数据: {}", item);
            // 业务逻辑
        }
    }
}
```

## 工作原理

1. 应用启动时，`XxlJobConfiguration` 会创建 `XxlJobSpringExecutor` 实例，用于与 XXL-JOB 调度中心通信。

2. `XxlJobAutoRegister` 组件会在应用准备就绪后：
   - 调用 `XxlJobLoginService` 登录到 XXL-JOB 调度中心获取 Cookie
   - 通过 `XxlJobGroupService` 检查并自动注册执行器
   - 扫描应用中所有标记了 `@XxlJob` 和 `@XxlJobRegister` 的方法
   - 通过 `XxlJobInfoService` 将这些任务注册到调度中心

3. 任务注册完成后，即可在 XXL-JOB 调度中心看到自动注册的执行器和任务。

## 注意事项

1. 确保 `xxl-job.enable=true` 以启用 XXL-JOB。
2. 必须配置 `admin-addresses`、`app-name`、`log-path`、`log-retention-days` 和 `port` 等必要参数。
3. 为了能够自动注册任务，必须配置 `title`、`user-name` 和 `password` 参数。
4. 任务的 `triggerStatus` 默认为 0（停止），如需启动后立即执行，请设置为 1（运行）。

## 常见问题

1. **连接调度中心失败**  
   检查 `admin-addresses` 是否正确，确保网络连通性。

2. **登录失败**  
   检查 `user-name` 和 `password` 是否与调度中心配置一致。

3. **执行器注册失败**  
   检查执行器 `app-name` 和 `title` 是否符合规范，是否已存在同名执行器。

4. **任务注册失败**  
   检查 `@XxlJobRegister` 注解参数是否正确，是否与 `@XxlJob` 一起使用。

## 许可证

Apache License 2.0 