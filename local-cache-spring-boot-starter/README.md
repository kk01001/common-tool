# 本地缓存 Spring Boot Starter

[![Maven Central](https://img.shields.io/maven-central/v/io.github.archer099/local-cache-spring-boot-starter.svg?style=flat-square)](https://search.maven.org/artifact/io.github.archer099/local-cache-spring-boot-starter)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg?style=flat-square)](http://www.apache.org/licenses/LICENSE-2.0.html)

**一句话概述：** 基于 [Caffeine](https://github.com/ben-manes/caffeine) 的轻量级本地缓存组件，为 Spring Boot 应用提供高性能的内存缓存解决方案。

## 背景

在现代应用开发中，缓存是提升系统性能的关键技术。相比较于分布式缓存（如Redis），本地缓存具有访问延迟低、无网络开销、部署简单等优势，特别适合单节点应用或对性能要求极高的场景。

`local-cache-spring-boot-starter` 基于业界领先的 Java 缓存库 Caffeine 开发，提供了与 Spring Boot 无缝集成的本地缓存解决方案。Caffeine 是一个高性能的缓存库，具有优秀的命中率和极低的延迟，其性能远超过传统的 Guava Cache 和 EhCache。

## 项目目标

- **高性能**：基于 Caffeine 提供近乎最优的缓存性能
- **易集成**：与 Spring Boot 应用无缝整合，开箱即用
- **低侵入**：统一缓存抽象，对业务代码无侵入
- **可扩展**：支持自定义缓存策略，满足不同场景需求
- **可监控**：提供丰富的缓存统计信息，便于监控和优化

## 核心功能与亮点 ✨

- **简洁抽象**：提供统一的缓存抽象，便于使用和管理
- **灵活配置**：支持自定义过期时间、最大容量、初始容量等参数
- **自动装配**：Spring Boot 自动配置，零配置即可使用
- **缓存工厂**：统一管理所有缓存实例，便于全局操作
- **性能监控**：提供命中率、加载时间等统计数据
- **事件监听**：支持缓存移除事件监听，便于资源释放和日志记录

## 技术栈 🛠️

- Java 21
- Spring Boot 3.x
- Caffeine 3.1.8

## 快速开始 🚀

### 添加依赖

```xml
<dependency>
    <groupId>io.github.archer099</groupId>
    <artifactId>local-cache-spring-boot-starter</artifactId>
    <version>${latest.version}</version>
</dependency>
```

### 创建缓存类

创建一个继承自 `AbstractLocalCaffeineCache` 的类，实现抽象方法：

```java
/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description 用户信息缓存
 */
@Component
@Slf4j
public class UserCache extends AbstractLocalCaffeineCache<Long, UserDTO> {

    /**
     * 获取缓存过期时间
     */
    @Override
    protected Duration getExpireAfterAccess() {
        // 30分钟后过期
        return Duration.ofMinutes(30);
    }

    /**
     * 获取最大缓存条数
     */
    @Override
    protected long getMaximumSize() {
        // 最多缓存10000条
        return 10000;
    }

    /**
     * 获取初始容量
     */
    @Override
    protected int getInitialCapacity() {
        // 初始容量为100
        return 100;
    }

    /**
     * 缓存移除监听器
     */
    @Override
    protected void onRemoval(Long userId, UserDTO userDTO, RemovalCause cause) {
        log.debug("User cache removed: userId={}, cause={}", userId, cause);
    }
}
```

### 使用缓存

```java
/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description 用户服务
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    
    /**
     * 获取用户信息（使用缓存）
     */
    public UserDTO getUserById(Long userId) {
        // 从缓存获取用户，如果不存在则从数据库加载
        return LocalCaffeineCacheFactory.getCache(UserCache.class)
                .get(userId, () -> loadUserFromDb(userId));
    }
    
    /**
     * 从数据库加载用户信息
     */
    private UserDTO loadUserFromDb(Long userId) {
        // 从数据库查询用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            return null;
        }
        
        // 转换为DTO并返回
        return UserDTO.fromEntity(user);
    }
    
    /**
     * 更新用户信息（同时更新缓存）
     */
    @Transactional
    public void updateUser(UserDTO userDTO) {
        // 更新数据库
        userMapper.updateById(User.fromDTO(userDTO));
        
        // 更新缓存
        LocalCaffeineCacheFactory.getCache(UserCache.class)
                .put(userDTO.getId(), userDTO);
    }
    
    /**
     * 删除用户（同时清除缓存）
     */
    @Transactional
    public void deleteUser(Long userId) {
        // 删除数据库记录
        userMapper.deleteById(userId);
        
        // 清除缓存
        LocalCaffeineCacheFactory.getCache(UserCache.class)
                .remove(userId);
    }
}
```

## 高级用法

### 1. 创建多级缓存

可以创建不同过期时间和容量的缓存实例，用于不同的业务场景：

```java
/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description 商品缓存（短期）
 */
@Component
public class ProductShortTermCache extends AbstractLocalCaffeineCache<String, ProductDTO> {
    @Override
    protected Duration getExpireAfterAccess() {
        return Duration.ofMinutes(5);  // 短期缓存，5分钟过期
    }
    
    @Override
    protected long getMaximumSize() {
        return 1000;  // 容量较小
    }
    
    @Override
    protected int getInitialCapacity() {
        return 100;
    }
    
    @Override
    protected void onRemoval(String key, ProductDTO value, RemovalCause cause) {
        // 处理缓存移除事件
    }
}

/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description 商品缓存（长期）
 */
@Component
public class ProductLongTermCache extends AbstractLocalCaffeineCache<String, ProductDTO> {
    @Override
    protected Duration getExpireAfterAccess() {
        return Duration.ofHours(2);  // 长期缓存，2小时过期
    }
    
    @Override
    protected long getMaximumSize() {
        return 10000;  // 容量较大
    }
    
    @Override
    protected int getInitialCapacity() {
        return 1000;
    }
    
    @Override
    protected void onRemoval(String key, ProductDTO value, RemovalCause cause) {
        // 处理缓存移除事件
    }
}
```

### 2. 缓存统计和监控

可以利用 `LocalCaffeineCacheFactory.getCacheStats()` 获取所有缓存的统计信息：

```java
/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description 缓存监控服务
 */
@Service
@Slf4j
public class CacheMonitorService {

    /**
     * 定时记录缓存统计信息
     */
    @Scheduled(fixedRate = 60000)  // 每分钟执行一次
    public void logCacheStats() {
        Map<String, Map<String, Object>> stats = LocalCaffeineCacheFactory.getCacheStats();
        
        stats.forEach((cacheName, cacheStats) -> {
            log.info("Cache [{}] stats: size={}, hitRate={}, missRate={}, evictionCount={}",
                    cacheName,
                    cacheStats.get("size"),
                    cacheStats.get("hitRate"),
                    cacheStats.get("missRate"),
                    cacheStats.get("evictionCount"));
        });
    }
    
    /**
     * 获取缓存统计信息API
     */
    public Map<String, Map<String, Object>> getCacheStats() {
        return LocalCaffeineCacheFactory.getCacheStats();
    }
}
```

### 3. 全局缓存操作

在某些场景下，可能需要对所有缓存进行操作，例如在配置变更或系统重启时：

```java
/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description 系统管理服务
 */
@Service
public class SystemService {

    /**
     * 清空所有缓存
     */
    public void clearAllCaches() {
        LocalCaffeineCacheFactory.clearAllCaches();
    }
    
    /**
     * 获取所有缓存实例
     */
    public Map<Class<?>, AbstractLocalCaffeineCache<?, ?>> getAllCaches() {
        return LocalCaffeineCacheFactory.getAllCaches();
    }
}
```

### 4. 结合Spring Cache使用

可以将本地缓存与Spring Cache注解结合使用：

```java
/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description 本地缓存管理器配置
 */
@Configuration
public class CacheConfig extends CachingConfigurerSupport {

    @Bean
    public CacheManager caffeineCacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        
        // 配置各种缓存
        List<CaffeineCache> caches = new ArrayList<>();
        
        // 用户缓存
        caches.add(new CaffeineCache("users",
                Caffeine.newBuilder()
                        .expireAfterAccess(Duration.ofMinutes(30))
                        .maximumSize(10000)
                        .build()));
        
        // 商品缓存
        caches.add(new CaffeineCache("products", 
                Caffeine.newBuilder()
                        .expireAfterAccess(Duration.ofMinutes(15))
                        .maximumSize(1000)
                        .build()));
        
        cacheManager.setCaches(caches);
        return cacheManager;
    }
}

/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description 使用Spring Cache注解
 */
@Service
public class ProductService {

    @Cacheable(value = "products", key = "#productId")
    public ProductDTO getProduct(Long productId) {
        // 从数据库加载商品
        return loadProductFromDb(productId);
    }
    
    @CachePut(value = "products", key = "#product.id")
    public ProductDTO updateProduct(ProductDTO product) {
        // 更新数据库
        saveProductToDb(product);
        return product;
    }
    
    @CacheEvict(value = "products", key = "#productId")
    public void deleteProduct(Long productId) {
        // 从数据库删除商品
        removeProductFromDb(productId);
    }
    
    // 其他方法...
}
```

## 性能优化

本地缓存性能已经相当优秀，但仍有一些优化技巧可以进一步提升性能：

### 1. 合理设置初始容量

如果预先知道缓存大约会存储多少元素，设置适当的初始容量可以减少缓存扩容的次数，提高性能：

```java
@Override
protected int getInitialCapacity() {
    // 如果预计会缓存约5000个元素，可以设置初始容量为接近的值
    return 5000;
}
```

### 2. 避免缓存穿透

对于可能不存在的数据，也应该缓存null值，避免频繁访问数据库：

```java
public UserDTO getUserById(Long userId) {
    // 即使数据库返回null也会被缓存，防止缓存穿透
    return LocalCaffeineCacheFactory.getCache(UserCache.class)
            .get(userId, () -> {
                UserDTO user = loadUserFromDb(userId);
                // 可以在这里返回null，Caffeine会缓存null值
                return user;
            });
}
```

### 3. 使用记录访问时间而非写入时间的过期策略

在大多数场景下，`expireAfterAccess`（基于访问时间过期）比`expireAfterWrite`（基于写入时间过期）更合适，因为它可以保留热点数据：

```java
@Override
protected Cache<K, V> createCache() {
    return Caffeine.newBuilder()
            .expireAfterAccess(getExpireAfterAccess())  // 基于访问时间过期，保留热点数据
            // .expireAfterWrite(Duration.ofMinutes(60))  // 基于写入时间过期，适合数据有时效性的场景
            .maximumSize(getMaximumSize())
            .initialCapacity(getInitialCapacity())
            .removalListener(this::onRemoval)
            .build();
}
```

## 应用场景

本地缓存在以下场景中特别有用：

- **高频查询数据**：如用户信息、商品信息等经常被查询但不常变动的数据
- **字典和配置数据**：系统配置、数据字典等相对静态的数据
- **API限流**：缓存请求频率，实现API限流
- **会话数据**：用户会话信息，特别是在单体应用中
- **计算结果缓存**：缓存耗时计算的结果，如复杂统计分析的结果

## 常见问题

### 1. 缓存一致性如何保证？

本地缓存主要适用于对一致性要求不高的场景。如果应用是集群部署，且对数据一致性要求高，建议：

1. 设置较短的缓存过期时间
2. 在数据变更时，通过消息队列通知其他节点清除缓存
3. 对于强一致性要求的场景，考虑使用分布式缓存如Redis

```java
/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description 缓存更新监听器
 */
@Component
@RequiredArgsConstructor
public class CacheUpdateListener {

    private final UserCache userCache;
    
    /**
     * 监听用户更新事件，清除本地缓存
     */
    @EventListener
    public void handleUserUpdated(UserUpdatedEvent event) {
        userCache.remove(event.getUserId());
    }
}
```

### 2. 如何防止缓存击穿？

缓存击穿是指对于某个热点key，在缓存过期的瞬间，大量请求同时访问该key。解决方案：

```java
/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description 防止缓存击穿的锁
 */
@Service
public class CacheProtectedService {

    private final Map<String, Object> locks = new ConcurrentHashMap<>();
    private final UserCache userCache;
    
    @Autowired
    public CacheProtectedService(UserCache userCache) {
        this.userCache = userCache;
    }
    
    /**
     * 使用锁防止缓存击穿
     */
    public UserDTO getUserWithProtection(Long userId) {
        // 先尝试从缓存获取
        UserDTO user = userCache.get(userId);
        if (user != null) {
            return user;
        }
        
        // 获取该用户ID的锁对象
        String lockKey = "user_" + userId;
        Object lock = locks.computeIfAbsent(lockKey, k -> new Object());
        
        // 使用同步块确保只有一个线程加载数据
        synchronized (lock) {
            // 再次检查缓存
            user = userCache.get(userId);
            if (user != null) {
                return user;
            }
            
            // 从数据库加载并放入缓存
            user = loadUserFromDb(userId);
            if (user != null) {
                userCache.put(userId, user);
            }
            return user;
        }
    }
    
    private UserDTO loadUserFromDb(Long userId) {
        // 从数据库加载用户
        return null; // 实际实现省略
    }
}
```

### 3. 缓存空间占用过大怎么办？

Caffeine缓存提供了基于大小和权重的淘汰策略：

```java
@Override
protected Cache<K, V> createCache() {
    return Caffeine.newBuilder()
            .expireAfterAccess(getExpireAfterAccess())
            .maximumSize(getMaximumSize())
            // 或者使用权重淘汰
            // .weigher((key, value) -> calculateWeight(key, value))
            // .maximumWeight(getMaximumWeight())
            .initialCapacity(getInitialCapacity())
            .removalListener(this::onRemoval)
            .build();
}

/**
 * 计算缓存条目的权重
 */
private int calculateWeight(K key, V value) {
    // 可以根据对象大小估算权重
    if (value instanceof String) {
        return ((String) value).length();
    }
    // 其他类型的默认权重
    return 1;
}
```

## 贡献 🙏

欢迎提交Issue或Pull Request参与项目贡献！

## 许可证

本项目使用 [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0.html) 许可证。

## 致谢

本项目基于 [Caffeine](https://github.com/ben-manes/caffeine) 开发，感谢 Caffeine 项目团队提供的优秀高性能缓存库。 