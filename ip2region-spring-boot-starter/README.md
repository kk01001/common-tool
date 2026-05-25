# IP地址查询 Spring Boot Starter

[![Maven Central](https://img.shields.io/maven-central/v/io.github.archer099/ip2region-spring-boot-starter.svg?style=flat-square)](https://search.maven.org/artifact/io.github.archer099/ip2region-spring-boot-starter)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg?style=flat-square)](http://www.apache.org/licenses/LICENSE-2.0.html)

**一句话概述：** 基于 [ip2region](https://github.com/lionsoul2014/ip2region) 的 Spring Boot Starter，提供便捷的 IP 地址归属地查询服务，无需连接互联网，支持离线查询。

## 背景

在互联网应用开发中，经常需要根据用户的 IP 地址获取其所在的地理位置信息，例如国家、省份、城市等。这些信息可以用于用户行为分析、安全审计、地域统计等多种场景。

传统的 IP 地址查询方式通常依赖在线第三方服务，存在接口调用限制、网络延迟、服务不稳定等问题。`ip2region-spring-boot-starter` 基于 [ip2region](https://github.com/lionsoul2014/ip2region) 项目，提供了一种离线查询 IP 地址归属地的解决方案，具有查询速度快、无网络依赖、可靠性高等优点。

## 项目目标

- **便捷集成**：与 Spring Boot 无缝集成，简单配置即可使用
- **高性能**：基于 xdb 引擎，提供毫秒级的查询性能
- **离线查询**：无需连接互联网，支持本地数据库查询
- **低内存消耗**：采用内存映射技术，减少内存占用
- **数据精确**：基于 ip2region 项目的高质量 IP 数据库

## 核心功能与亮点 ✨

- **自动配置**：Spring Boot 自动配置，开箱即用
- **简洁 API**：提供简单易用的查询接口
- **数据准确**：返回国家、地区、省份、城市、ISP 等完整信息
- **性能优越**：基于优化的 xdb 引擎，查询性能高
- **异常处理**：内置异常处理机制，提高系统稳定性

## 技术栈 🛠️

- Java 21
- Spring Boot 3.x
- ip2region 2.7.0

## 快速开始 🚀

### 添加依赖

```xml
<dependency>
    <groupId>io.github.archer099</groupId>
    <artifactId>ip2region-spring-boot-starter</artifactId>
    <version>${latest.version}</version>
</dependency>
```

### 配置属性

在 `application.yml` 或 `application.properties` 中添加以下配置：

```yaml
ip2region:
  # 是否启用IP地址查询功能
  enabled: true
  # IP数据库文件路径（必须配置）
  db-path: /path/to/your/ip2region.xdb
```

> 注意：你需要下载 ip2region 数据库文件。可以从 [ip2region 官方仓库](https://github.com/lionsoul2014/ip2region/tree/master/data) 获取 `ip2region.xdb` 文件。

### 使用示例

```java
/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description IP地址查询服务
 */
@Service
@RequiredArgsConstructor
public class IpLocationService {

    private final Ip2RegionTemplate ip2RegionTemplate;
    
    /**
     * 查询IP地址归属地
     */
    public RegionResult queryIpLocation(String ip) {
        return ip2RegionTemplate.search(ip);
    }
    
    /**
     * 获取用户IP所在城市
     */
    public String getUserCity(HttpServletRequest request) {
        String ip = getClientIp(request);
        RegionResult result = ip2RegionTemplate.search(ip);
        if (result != null) {
            return result.getProvince() + " " + result.getCity();
        }
        return "未知";
    }
    
    /**
     * 获取客户端真实IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多级代理的情况，第一个IP为客户端真实IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
```

### 在Controller中使用

```java
/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description IP地址查询控制器
 */
@RestController
@RequestMapping("/api/ip")
@RequiredArgsConstructor
public class IpController {

    private final Ip2RegionTemplate ip2RegionTemplate;
    
    /**
     * 查询IP归属地
     */
    @GetMapping("/query")
    public Result<RegionResult> queryIp(@RequestParam String ip) {
        RegionResult result = ip2RegionTemplate.search(ip);
        if (result != null) {
            return Result.success(result);
        }
        return Result.error("IP地址查询失败");
    }
    
    /**
     * 获取当前用户的IP归属地
     */
    @GetMapping("/current")
    public Result<RegionResult> getCurrentUserIpInfo(HttpServletRequest request) {
        String ip = getClientIp(request);
        RegionResult result = ip2RegionTemplate.search(ip);
        if (result != null) {
            return Result.success(result);
        }
        return Result.error("IP地址查询失败");
    }
    
    // getClientIp方法实现同上...
}
```

## 返回数据结构

`RegionResult` 包含以下字段：

| 字段 | 类型 | 说明 | 示例 |
| --- | --- | --- | --- |
| country | String | 国家 | 中国 |
| area | String | 地区 | 华东 |
| province | String | 省份 | 浙江省 |
| city | String | 城市 | 杭州市 |
| isp | String | 网络运营商 | 电信 |

示例返回值：

```json
{
  "country": "中国",
  "area": "华东",
  "province": "浙江省",
  "city": "杭州市",
  "isp": "电信"
}
```

## 高级用法

### 1. 使用AOP拦截器自动记录IP归属地

```java
/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description IP归属地记录切面
 */
@Aspect
@Component
@RequiredArgsConstructor
public class IpLogAspect {

    private final Ip2RegionTemplate ip2RegionTemplate;
    private final HttpServletRequest request;
    
    @Around("@annotation(org.springframework.web.bind.annotation.RequestMapping)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        String ip = getClientIp(request);
        RegionResult region = ip2RegionTemplate.search(ip);
        
        // 记录请求信息和IP归属地
        MDC.put("clientIp", ip);
        if (region != null) {
            MDC.put("ipLocation", region.getProvince() + "-" + region.getCity());
        }
        
        try {
            return joinPoint.proceed();
        } finally {
            MDC.remove("clientIp");
            MDC.remove("ipLocation");
        }
    }
    
    // getClientIp方法实现同上...
}
```

### 2. 自定义处理不同地域的用户

```java
/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description 地域处理服务
 */
@Service
@RequiredArgsConstructor
public class RegionService {

    private final Ip2RegionTemplate ip2RegionTemplate;
    
    /**
     * 根据用户IP判断是否境外用户
     */
    public boolean isOverseasUser(String ip) {
        RegionResult region = ip2RegionTemplate.search(ip);
        if (region != null) {
            return !"中国".equals(region.getCountry());
        }
        return false;
    }
    
    /**
     * 根据用户IP获取适合的服务器节点
     */
    public String getProperServerNode(String ip) {
        RegionResult region = ip2RegionTemplate.search(ip);
        if (region == null) {
            return "default-node";
        }
        
        // 根据地域信息选择最近的服务节点
        switch (region.getArea()) {
            case "华东":
                return "east-china-node";
            case "华南":
                return "south-china-node";
            case "华北":
                return "north-china-node";
            default:
                return "central-node";
        }
    }
}
```

### 3. 与Gateway集成实现地域路由

```java
/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description 基于IP地域的路由过滤器
 */
@Component
@RequiredArgsConstructor
public class IpBasedRouteFilter implements GlobalFilter, Ordered {

    private final Ip2RegionTemplate ip2RegionTemplate;
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String ip = getClientIp(exchange.getRequest());
        RegionResult region = ip2RegionTemplate.search(ip);
        
        if (region != null) {
            // 添加地域信息到请求头
            ServerHttpRequest request = exchange.getRequest().mutate()
                .header("X-User-Country", region.getCountry())
                .header("X-User-Province", region.getProvince())
                .header("X-User-City", region.getCity())
                .build();
                
            // 更新路由信息
            return chain.filter(exchange.mutate().request(request).build());
        }
        
        return chain.filter(exchange);
    }
    
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 100;
    }
    
    // getClientIp方法实现...
}
```

## 性能优化

ip2region 使用的 xdb 引擎已经做了许多性能优化，但仍有一些最佳实践可以进一步提升性能：

1. **内存映射**：默认情况下，ip2region 已使用内存映射优化查询性能

2. **缓存热点IP**：对于频繁查询的IP，可以实现二级缓存

```java
/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description 带缓存的IP查询服务
 */
@Service
public class CachedIpService {

    private final Ip2RegionTemplate ip2RegionTemplate;
    private final LoadingCache<String, RegionResult> ipCache;
    
    public CachedIpService(Ip2RegionTemplate ip2RegionTemplate) {
        this.ip2RegionTemplate = ip2RegionTemplate;
        
        // 创建Guava缓存
        this.ipCache = CacheBuilder.newBuilder()
            .maximumSize(10000)  // 最多缓存10000个IP
            .expireAfterWrite(1, TimeUnit.DAYS)  // 1天后过期
            .build(new CacheLoader<>() {
                @Override
                public RegionResult load(String ip) {
                    return ip2RegionTemplate.search(ip);
                }
            });
    }
    
    public RegionResult queryIp(String ip) {
        try {
            return ipCache.get(ip);
        } catch (ExecutionException e) {
            return ip2RegionTemplate.search(ip);
        }
    }
}
```

## 应用场景

- **用户行为分析**：根据用户IP所属地区分析用户行为特征
- **营销活动定向**：针对特定地域用户提供定制化的营销活动
- **内容分发优化**：根据用户地理位置选择最近的CDN节点
- **安全风控**：识别异常登录地点，提高账号安全性
- **访问控制**：限制特定地区用户的访问权限
- **数据合规**：满足不同地区数据保护法规的合规要求
- **日志分析**：在系统日志中记录用户IP归属地信息，便于后续分析

## 常见问题

### 1. 如何更新IP数据库？

ip2region 数据库文件需要定期更新以保持准确性。更新时，只需替换配置的数据库文件路径指向的文件即可。如果希望动态更新而不重启应用，可以实现以下方案：

```java
/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description IP数据库更新服务
 */
@Service
@RequiredArgsConstructor
public class Ip2RegionUpdater {

    private final Ip2RegionProperties properties;
    
    /**
     * 更新IP数据库
     */
    @Scheduled(cron = "0 0 3 1 * ?")  // 每月1日凌晨3点执行
    public void updateDatabase() throws Exception {
        String dbPath = properties.getDbPath();
        String backupPath = dbPath + ".bak";
        String newDbPath = dbPath + ".new";
        
        // 下载新数据库文件
        downloadLatestDatabase(newDbPath);
        
        // 备份旧文件
        Files.copy(Paths.get(dbPath), Paths.get(backupPath), StandardCopyOption.REPLACE_EXISTING);
        
        // 替换数据库文件
        Files.move(Paths.get(newDbPath), Paths.get(dbPath), StandardCopyOption.REPLACE_EXISTING);
        
        // 重新加载Searcher
        reloadSearcher();
    }
    
    private void downloadLatestDatabase(String savePath) {
        // 实现下载逻辑
    }
    
    private void reloadSearcher() {
        // 重新加载搜索器
    }
}
```

### 2. IP2Region 数据库的准确性如何？

ip2region 的数据库准确性相对较高，但由于 IP 地址分配和网络拓扑结构的变化，可能存在一定误差。对于需要高精度定位的场景，建议结合多种数据源进行交叉验证。

### 3. 私有网络和内网IP如何处理？

对于私有网络IP（如192.168.x.x、10.x.x.x等），ip2region 会返回内网地址相关信息。在业务中应注意这类IP的特殊处理：

```java
/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description IP处理工具类
 */
public class IpUtils {

    /**
     * 判断是否为内网IP
     */
    public static boolean isInternalIp(String ip) {
        if (ip == null || ip.isEmpty()) {
            return true;
        }
        
        try {
            byte[] addr = InetAddress.getByName(ip).getAddress();
            return isInternalIp(addr);
        } catch (Exception e) {
            return false;
        }
    }
    
    private static boolean isInternalIp(byte[] addr) {
        if (addr.length != 4) {
            return false;
        }
        
        // 10.x.x.x
        if (addr[0] == 10) {
            return true;
        }
        
        // 172.16.x.x - 172.31.x.x
        if (addr[0] == (byte) 172 && (addr[1] >= 16 && addr[1] <= 31)) {
            return true;
        }
        
        // 192.168.x.x
        return addr[0] == (byte) 192 && addr[1] == (byte) 168;
    }
}
```

## 贡献 🙏

欢迎提交Issue或Pull Request参与项目贡献！

## 许可证

本项目使用 [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0.html) 许可证。

## 致谢

本项目基于 [ip2region](https://github.com/lionsoul2014/ip2region) 开发，感谢 ip2region 项目团队提供的优秀工作。 