# 设计模式 Spring Boot Starter

[![Maven Central](https://img.shields.io/maven-central/v/io.github.kk01001/design-pattern-spring-boot-starter.svg?style=flat-square)](https://search.maven.org/artifact/io.github.kk01001/design-pattern-spring-boot-starter)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg?style=flat-square)](http://www.apache.org/licenses/LICENSE-2.0.html)

**一句话概述:** 一个将常见设计模式封装为 Spring Boot Starter 的工具库，旨在简化 Java 开发中设计模式的应用，提高代码质量和开发效率。

## 背景

在 Java 应用开发中，设计模式是解决特定问题的成熟方案，但其实现往往因人而异，导致代码质量不一致。此外，重复实现这些模式也是一种资源浪费。

本项目旨在提供一套标准化、通用化、高质量的设计模式实现，让开发者通过简单的配置即可使用各种设计模式，从而专注于业务逻辑的开发。

## 项目目标

`design-pattern-spring-boot-starter` 项目旨在通过 Spring Boot 自动配置机制，将常用设计模式封装成可复用组件，提供以下价值：

- **标准化实现**：提供经过验证的设计模式标准实现
- **简化开发**：通过注解和工厂类简化设计模式的使用
- **提高效率**：减少样板代码，提高开发效率
- **增强可维护性**：统一的设计模式实现方式，提高代码可维护性

## 核心功能与亮点 ✨

- **多种设计模式支持**：支持策略模式、责任链模式、观察者模式、单例模式、状态机模式等常用设计模式
- **基于注解的配置**：通过注解快速定义和使用设计模式
- **工厂模式封装**：提供工厂类自动管理模式实例
- **Spring 集成**：充分利用 Spring 的依赖注入、AOP 等特性
- **分布式支持**：集成 Redisson，支持分布式场景下的设计模式应用
- **监控能力**：集成 Spring Boot Actuator 和 Micrometer，提供运行时监控
- **可扩展性**：提供清晰的接口和抽象类，方便扩展

## 技术栈 🛠️

- Java 21
- Spring Boot 3.x
- Maven
- Redisson
- Lombok
- Hutool
- Micrometer

## 快速开始 🚀

### 添加依赖

```xml
<dependency>
    <groupId>io.github.kk01001</groupId>
    <artifactId>design-pattern-spring-boot-starter</artifactId>
    <version>${latest.version}</version>
</dependency>
```

## 设计模式详解

### 1. 策略模式

策略模式定义了算法族，分别封装起来，让它们之间可以互相替换，此模式让算法的变化独立于使用算法的客户。

#### 功能介绍

- 基于注解的策略定义
- 基于枚举的策略分类
- 自动注册策略到工厂
- 支持异常处理

#### 应用场景

- 支付方式选择（支付宝、微信、银行卡）
- 多渠道消息发送（短信、邮件、推送）
- 多种算法实现（排序算法、推荐算法）
- 多种规则处理（风控规则、促销规则）

#### 使用示例

1. 定义策略枚举

```java
/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description 支付方式枚举
 */
public enum PaymentTypeEnum {
    ALIPAY,
    WECHAT,
    BANK_CARD
}
```

2. 定义策略接口

```java
/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description 支付策略接口
 */
public interface PaymentStrategy extends IStrategy<PaymentContext, Boolean> {
    @Override
    Boolean execute(PaymentContext context);
}
```

3. 实现具体策略

```java
/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description 支付宝支付策略
 */
@Component
@Strategy(strategyEnum = PaymentTypeEnum.class, strategyType = "ALIPAY")
public class AlipayStrategy implements PaymentStrategy {
    @Override
    public Boolean execute(PaymentContext context) {
        System.out.println("使用支付宝支付：" + context.getAmount());
        // 实现支付宝支付逻辑
        return true;
    }
}
```

4. 使用策略工厂

```java
/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description 支付服务
 */
@Service
public class PaymentService {
    private final StrategyFactory strategyFactory;
    
    public PaymentService(StrategyFactory strategyFactory) {
        this.strategyFactory = strategyFactory;
    }
    
    public boolean pay(String type, PaymentContext context) {
        return strategyFactory.execute(PaymentTypeEnum.class, type, context);
    }
}
```

### 2. 责任链模式

责任链模式为请求创建了一个接收者对象的链，每个接收者包含对另一个接收者的引用，如果一个对象不能处理该请求，那么它会把相同的请求传给下一个接收者。

#### 功能介绍

- 基于注解的责任链定义
- 支持处理器排序
- 链路上下文传递
- 支持异常处理
- 支持链路中断控制

#### 应用场景

- 请求过滤和鉴权（登录校验、权限校验）
- 多级审批流程（请假审批、报销审批）
- 日志处理链（日志收集、格式化、存储）
- 事件处理流程（订单创建、支付、发货）

#### 使用示例

1. 定义处理器

```java
/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description 认证处理器
 */
@Component
@ResponsibilityChain(value = "authChain", order = 1)
public class AuthenticationHandler implements ResponsibilityChainHandler<LoginRequest, LoginResult> {
    @Override
    public LoginResult handle(ResponsibilityChainContext<LoginRequest, LoginResult> context) throws Exception {
        LoginRequest request = context.getData();
        
        // 验证用户名和密码
        if (!"admin".equals(request.getUsername()) || !"password".equals(request.getPassword())) {
            context.setTerminated(true); // 中断链路
            return new LoginResult(false, "用户名或密码错误");
        }
        
        return null; // 继续下一个处理器
    }
}
```

2. 定义多个处理器形成责任链

```java
/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description 角色检查处理器
 */
@Component
@ResponsibilityChain(value = "authChain", order = 2)
public class RoleCheckHandler implements ResponsibilityChainHandler<LoginRequest, LoginResult> {
    @Override
    public LoginResult handle(ResponsibilityChainContext<LoginRequest, LoginResult> context) throws Exception {
        LoginRequest request = context.getData();
        
        // 检查用户角色
        if (!hasPermission(request.getUsername())) {
            context.setTerminated(true);
            return new LoginResult(false, "用户无权限");
        }
        
        return null;
    }
    
    private boolean hasPermission(String username) {
        // 实现权限检查逻辑
        return true;
    }
}
```

3. 使用责任链工厂

```java
/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description 登录服务
 */
@Service
public class LoginService {
    private final ResponsibilityChainFactory chainFactory;
    
    public LoginService(ResponsibilityChainFactory chainFactory) {
        this.chainFactory = chainFactory;
    }
    
    public LoginResult login(LoginRequest request) {
        // 创建责任链上下文
        ResponsibilityChainContext<LoginRequest, LoginResult> context = 
            ResponsibilityChainContext.<LoginRequest, LoginResult>builder()
                .data(request)
                .build();
        
        // 执行责任链
        LoginResult result = chainFactory.execute("authChain", context);
        
        // 如果责任链没有返回结果（全部处理器执行完毕），则返回成功结果
        if (result == null) {
            return new LoginResult(true, "登录成功");
        }
        
        return result;
    }
}
```

### 3. 观察者模式

观察者模式定义了对象之间的一对多依赖，当一个对象的状态发生改变时，它的所有依赖者都会收到通知并自动更新。

#### 功能介绍

- 基于注解的观察者定义
- 支持观察者优先级排序
- 主题-观察者自动关联
- 异步通知支持

#### 应用场景

- 事件通知（用户注册事件、订单支付事件）
- 状态变更监听（订单状态变更、库存变更）
- 日志记录（操作日志、审计日志）
- 缓存更新（数据变更后更新缓存）

#### 使用示例

1. 定义主题（事件）

```java
/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description 订单支付成功事件
 */
public class OrderPaidEvent extends Subject {
    private final String orderId;
    private final BigDecimal amount;
    
    public OrderPaidEvent(Object source, String orderId, BigDecimal amount) {
        super(source);
        this.orderId = orderId;
        this.amount = amount;
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    @Override
    public String getTopic() {
        return "ORDER_PAID";
    }
}
```

2. 实现观察者

```java
/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description 发送短信的观察者
 */
@Component
@io.github.kk01001.design.pattern.observer.annotation.Observer(topic = "ORDER_PAID")
public class SmsNotificationObserver implements IObserver<OrderPaidEvent> {
    @Override
    public void onUpdate(OrderPaidEvent event) {
        System.out.println("发送短信通知，订单已支付：" + event.getOrderId());
        // 实现发送短信逻辑
    }
    
    @Override
    public int getOrder() {
        return 1; // 优先级高
    }
}
```

3. 使用观察者工厂

```java
/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description 订单服务
 */
@Service
public class OrderService {
    private final ObserverFactory observerFactory;
    
    public OrderService(ObserverFactory observerFactory) {
        this.observerFactory = observerFactory;
    }
    
    public void payOrder(String orderId, BigDecimal amount) {
        // 处理订单支付逻辑...
        
        // 创建订单支付事件
        OrderPaidEvent event = new OrderPaidEvent(this, orderId, amount);
        
        // 通知所有观察者
        observerFactory.notify(event);
    }
}
```

### 4. 单例模式

单例模式确保一个类只有一个实例，并提供一个全局访问点。

#### 功能介绍

- 支持多种单例实现方式（懒加载、饿汉式、双重检查、枚举）
- 通过注解简化单例定义
- 自动注册到单例工厂

#### 应用场景

- 配置管理器（系统配置、应用配置）
- 连接池管理（数据库连接池、线程池）
- 缓存管理（本地缓存、共享缓存）
- 日志管理（日志记录器）

#### 使用示例

1. 使用注解定义单例

```java
/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description 配置管理器
 */
@SingletonPattern(type = SingletonPattern.Type.LAZY)
public class ConfigManager {
    private Map<String, String> configs = new HashMap<>();
    
    private ConfigManager() {
        // 私有构造函数
        loadConfigs();
    }
    
    private void loadConfigs() {
        // 加载配置
        configs.put("app.name", "MyApp");
        configs.put("app.version", "1.0.0");
    }
    
    public String getConfig(String key) {
        return configs.get(key);
    }
    
    public void setConfig(String key, String value) {
        configs.put(key, value);
    }
}
```

2. 使用单例工厂

```java
/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description 使用单例
 */
@Service
public class AppService {
    private final SingletonFactory singletonFactory;
    
    public AppService(SingletonFactory singletonFactory) {
        this.singletonFactory = singletonFactory;
    }
    
    public String getAppConfig(String key) {
        // 获取单例实例
        ConfigManager configManager = singletonFactory.getInstance(ConfigManager.class);
        return configManager.getConfig(key);
    }
}
```

### 5. 状态机模式

状态机模式允许一个对象在其内部状态改变时改变它的行为，对象看起来似乎修改了它的类。

#### 功能介绍

- 基于注解的状态机定义
- 丰富的状态转换控制
- 守卫条件支持
- 状态持久化
- 状态转换事件
- 状态转换历史记录
- 监控指标

#### 应用场景

- 订单流程（创建、支付、发货、收货、评价）
- 工作流引擎（审批流程、业务流程）
- 游戏状态（空闲、战斗、死亡）
- 文档状态（草稿、审核中、已发布）

#### 使用示例

1. 定义状态和事件

```java
/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description 订单状态
 */
public enum OrderState {
    CREATED,    // 已创建
    PAID,       // 已支付
    SHIPPED,    // 已发货
    DELIVERED,  // 已送达
    COMPLETED,  // 已完成
    CANCELLED   // 已取消
}

/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description 订单事件
 */
public enum OrderEvent {
    PAY,        // 支付
    SHIP,       // 发货
    DELIVER,    // 送达
    CONFIRM,    // 确认收货
    CANCEL      // 取消
}
```

2. 定义状态机

```java
/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description 订单状态机
 */
@Component
@StateMachineDefinition(
    name = "orderStateMachine",
    stateClass = OrderState.class,
    initialState = "CREATED"
)
public class OrderStateMachine {
    
    @StateTransition(source = "CREATED", target = "PAID", event = "PAY")
    @TransitionGuard(spEL = {"#context.hasSufficientStock()"})
    public void payOrder(OrderState source, OrderState target, OrderEvent event, OrderContext context) {
        System.out.println("订单支付，状态从 " + source + " 变更为 " + target);
        // 执行支付逻辑
    }
    
    @StateTransition(source = "PAID", target = "SHIPPED", event = "SHIP")
    public void shipOrder(OrderState source, OrderState target, OrderEvent event, OrderContext context) {
        System.out.println("订单发货，状态从 " + source + " 变更为 " + target);
        // 执行发货逻辑
    }
    
    @StateTransition(source = "SHIPPED", target = "DELIVERED", event = "DELIVER")
    public void deliverOrder(OrderState source, OrderState target, OrderEvent event, OrderContext context) {
        System.out.println("订单送达，状态从 " + source + " 变更为 " + target);
        // 执行送达逻辑
    }
    
    @StateTransition(source = "DELIVERED", target = "COMPLETED", event = "CONFIRM")
    public void confirmOrder(OrderState source, OrderState target, OrderEvent event, OrderContext context) {
        System.out.println("确认收货，状态从 " + source + " 变更为 " + target);
        // 执行确认收货逻辑
    }
    
    @StateTransition(source = {"CREATED", "PAID"}, target = "CANCELLED", event = "CANCEL")
    public void cancelOrder(OrderState source, OrderState target, OrderEvent event, OrderContext context) {
        System.out.println("取消订单，状态从 " + source + " 变更为 " + target);
        // 执行取消订单逻辑
    }
}
```

3. 使用状态机

```java
/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description 订单服务
 */
@Service
public class OrderService {
    private final StateMachineFactory stateMachineFactory;
    
    public OrderService(StateMachineFactory stateMachineFactory) {
        this.stateMachineFactory = stateMachineFactory;
    }
    
    public void payOrder(String orderId) {
        // 获取状态机
        StateMachine<OrderState, OrderEvent, OrderContext> stateMachine = 
            stateMachineFactory.getStateMachine("orderStateMachine");
        
        // 创建上下文
        OrderContext context = new OrderContext(orderId);
        
        // 启动状态机
        stateMachine.start(orderId, context);
        
        // 发送支付事件
        OrderState newState = stateMachine.sendEvent(orderId, OrderEvent.PAY, context);
        
        System.out.println("订单新状态：" + newState);
    }
}
```

## 贡献 🙏

欢迎提交 Issue 或 Pull Request 参与项目贡献！

## 许可证

本项目使用 [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0.html) 许可证。 