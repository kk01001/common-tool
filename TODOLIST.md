# Starter 功能列表

## common-tool-spring-boot-starter
  > 通用工具集合
  - [x] 分布式id 
  - [x] 字典翻译
  - [x] i18n 国际化
  - [x] 接口操作日志封装
  - [x] 事务util

## multi-redis-spring-boot-starter
  > 多Redis实例支持
  - [x] Redisson 多数据源配置

## mybatis-plus-spring3-boot-starter
  > MyBatis-Plus增强
  - [x] Mybatis Plus 拦截器配置
  - [x] 动态表名拦截器

## xxl-job-spring-boot-starter
  > XXL-JOB任务调度
  - [x] 自动注册执行器
  - [x] 自动注册任务

## oss-spring-boot-starter
  > 对象存储服务
  - [x] S3多种存储平台支持
  - [x] 统一API

## sftp-spring-boot-starter
  > SFTP连接池
  - [x] 连接池管理
  - [x] 文件传输

## dynamic-mq-spring-boot-starter
  > 动态消息队列
  - [ ] 多种MQ支持
  - [ ] 动态切换（根据配置）

## rate-limiter-spring-boot-starter
  > 单机/分布式限流器
  - [x] 多种限流策略 Guava Redis Redisson Lua
  - [x] 限流注解 @RateLimiter
  - [ ] Sentinel限流

## lock-spring-boot-starter
  > 分布式锁
  - [x] 锁注解 @Lock
  - [x] Redis Redisson Lua

## idempotent-spring-boot-starter
  > 幂等性控制
  - [x] 幂等注解 @Idempotent
  - [x] 自定义幂等key生成器

## ip2region-spring-boot-starter
  > IP地址查询
  - [x] 快速查询IP归属地

## robot-message-spring-boot-starter
  > 机器人消息
  - [x] 钉钉支持
  - [x] 企业微信支持
  - [x] 短信, 支持Groovy脚本
  - [x] 邮件

## docs-spring-boot-starter
  > 接口文档
  - [x] 在线接口文档

## excel-spring-boot-starter
  > Excel处理
  - [x] 简单导入导出
  - [x] 导出模板方法
  - [x] 导出压缩文件, 多个Excel文件压缩
  - [x] 导出Excel文件, 一个Excel文件多个Sheet
  - [x] 大数据量读取入库

## netty-spring-boot-starter
  > Netty网络框架
  - [x] 注解驱动
  - [x] 多端点配置
  - [x] 灵活的消息处理
  - [x] 完整的生命周期管理
  - [x] 支持会话属性
  - [x] 自定义消息类型
  - [x] 配置化支持
  - [x] 集群扩展
  - [x] 心跳检测
  - [x] SSL支持
  - [x] 集群支持
  - [x] 消息广播
  - [ ] 消息编解码器
  - [ ] 路径匹配
  - [ ] 拦截器链

## crypto-spring-boot-starter
  > 加密解密
  - [x] 接口参数加密 @ParamsCrypto
  - [x] 接口返回值加密 @ParamsCrypto
  - [x] 数据库字段加密 @CryptoField

## desensitize-spring-boot-starter
  > 数据脱敏
  - [x] 数据脱敏注解 @Desensitize
  - [x] 脱敏util DesensitizeUtil
  - [x] Jackson 序列化自动脱敏
  - [x] FastJson 序列化自动脱敏
  - [x] 支持自定义脱敏处理器

## disruptor-spring-boot-starter
  > 高性能队列
  - [x] 单消费者
  - [x] 同步发送
  - [x] 单生产者/多生产者
  - [x] 手动创建队列和消费者(单消费者)
  - [ ] 支持广播
  - [ ] 多消费者
  - [ ] 支持动态修改RingBuffer大小等
  - [ ] 异步发送
  - [ ] 发送结果回调

## script-spring-boot-starter
  > 脚本执行(待测试)
  - [x] 支持多种脚本语言
  - [x] Groovy
  - [x] JavaScript
  - [x] Python
  - [x] Java

## push-spring-boot-starter (计划中)
  > 推送服务
  - [ ] 多平台推送支持

## dict-spring-boot-starter
  > 字典管理

## design-pattern-spring-boot-starter
  > 设计模式
  - [x] 策略模式
  - [ ] 责任链模式

# 待开发功能

- [ ] nacos数据库配置动态刷新
- [ ] 配置加密
- [ ] 日志脱敏
- [ ] 通用支付（微信、支付宝）
- [ ] 验证码（图形验证码、短信验证码等）
- [ ] 动态线程池
- [ ] 数据权限