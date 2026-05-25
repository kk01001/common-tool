# SFTP Spring Boot Starter

[![Maven Central](https://img.shields.io/maven-central/v/io.github.archer099/sftp-spring-boot-starter.svg)](https://search.maven.org/artifact/io.github.archer099/sftp-spring-boot-starter)
[![License](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.txt)

## 简介

`sftp-spring-boot-starter` 是一个为 Spring Boot 应用提供 SFTP 连接池管理的组件。该组件基于 JSch 和 Apache Commons Pool2 实现，提供高效、可靠的 SFTP 连接管理，支持多连接池配置，适用于需要频繁与 SFTP 服务器交互的应用场景。

## 功能特点

- 🔌 **连接池管理**：基于 Apache Commons Pool2 的高效 SFTP 连接池实现
- 🧩 **多池支持**：支持同时管理多个不同配置的 SFTP 连接池
- 📦 **自动装配**：基于 Spring Boot 自动配置，零代码即可集成
- 💾 **资源优化**：自动管理连接的创建、验证、回收和销毁
- 🔒 **线程安全**：线程安全的连接池实现，适用于并发场景
- 🧠 **参数配置**：灵活的连接池参数配置，包括最大连接数、空闲连接等
- 🔍 **状态监控**：提供连接池状态监控和日志记录

## 技术栈

- Java 21
- Spring Boot 3.x
- JSch（Java Secure Channel）
- Apache Commons Pool2
- Hutool

## 快速开始

### 添加依赖

在你的 `pom.xml` 文件中添加以下依赖：

```xml
<dependency>
    <groupId>io.github.archer099</groupId>
    <artifactId>sftp-spring-boot-starter</artifactId>
    <version>最新版本</version>
</dependency>
```

### 配置属性

在 `application.properties` 或 `application.yml` 中配置：

```yaml
sftp-pool:
  # 是否启用SFTP连接池
  enable: true
  # SFTP服务器主机
  host: sftp.example.com
  # SFTP服务器端口
  port: 22
  # 用户名
  username: ftpuser
  # 密码
  password: ftppassword
  # 连接超时时间(秒)
  connect-timeout: 3s
  # 空闲连接驱逐时间(秒)
  min-evictable-idle-time: 5s
  # 最大空闲连接数
  max-idle: 3
  # 最小空闲连接数
  min-idle: 1
  # 最大连接总数
  max-total: 3
```

### 注入连接池

```java
import com.jcraft.jsch.ChannelSftp;
import io.github.archer099.sftp.core.JschConnectionPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SftpService {

    @Autowired
    private JschConnectionPool jschConnectionPool;
    
    // 使用SFTP连接池...
}
```

## 使用示例

### 基本使用

```java
import com.jcraft.jsch.ChannelSftp;
import io.github.archer099.sftp.core.JschConnectionPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.InputStream;

@Service
public class SftpService {

    @Autowired
    private JschConnectionPool jschConnectionPool;
    
    /**
     * 上传文件到SFTP服务器
     */
    public void uploadFile(String localFilePath, String remoteFilePath) throws Exception {
        ChannelSftp channelSftp = null;
        try {
            // 从连接池获取SFTP连接
            channelSftp = jschConnectionPool.borrowObject();
            
            // 上传文件
            try (InputStream inputStream = new FileInputStream(localFilePath)) {
                channelSftp.put(inputStream, remoteFilePath);
            }
        } finally {
            // 归还连接到连接池
            if (channelSftp != null) {
                jschConnectionPool.returnObject(channelSftp);
            }
        }
    }
    
    /**
     * 从SFTP服务器下载文件
     */
    public void downloadFile(String remoteFilePath, String localFilePath) throws Exception {
        ChannelSftp channelSftp = null;
        try {
            // 从连接池获取SFTP连接
            channelSftp = jschConnectionPool.borrowObject();
            
            // 下载文件
            channelSftp.get(remoteFilePath, localFilePath);
        } finally {
            // 归还连接到连接池
            if (channelSftp != null) {
                jschConnectionPool.returnObject(channelSftp);
            }
        }
    }
}
```

### 多连接池管理

```java
import com.jcraft.jsch.ChannelSftp;
import io.github.archer099.sftp.core.JschConnectionPool;
import io.github.archer099.sftp.core.SftpInfoProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class MultiPoolSftpService {

    @Autowired
    private JschConnectionPool jschConnectionPool;
    
    /**
     * 创建并使用自定义连接池
     */
    public void useCustomPool() throws Exception {
        // 创建自定义SFTP连接池配置
        SftpInfoProperties customProperties = SftpInfoProperties.builder()
                .host("another-sftp.example.com")
                .port(22)
                .username("customuser")
                .password("custompass")
                .maxTotal(5)
                .minIdle(2)
                .maxIdle(5)
                .connectTimeout(Duration.ofSeconds(5))
                .minEvictableIdleTime(Duration.ofSeconds(10))
                .build();
        
        // 构建自定义连接池
        String poolKey = "custom-pool";
        jschConnectionPool.buildPool(poolKey, customProperties);
        
        // 使用自定义连接池
        ChannelSftp channelSftp = null;
        try {
            channelSftp = jschConnectionPool.borrowObject(poolKey);
            // 执行SFTP操作...
            channelSftp.cd("/remote/directory");
            // ... 其他操作
        } finally {
            if (channelSftp != null) {
                jschConnectionPool.returnObject(poolKey, channelSftp);
            }
        }
    }
}
```

### 目录操作

```java
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import io.github.archer099.sftp.core.JschConnectionPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Vector;

@Service
public class SftpDirectoryService {

    @Autowired
    private JschConnectionPool jschConnectionPool;
    
    /**
     * 列出远程目录文件
     */
    @SuppressWarnings("unchecked")
    public Vector<ChannelSftp.LsEntry> listDirectory(String remoteDir) throws Exception {
        ChannelSftp channelSftp = null;
        try {
            channelSftp = jschConnectionPool.borrowObject();
            return channelSftp.ls(remoteDir);
        } finally {
            if (channelSftp != null) {
                jschConnectionPool.returnObject(channelSftp);
            }
        }
    }
    
    /**
     * 创建远程目录
     */
    public void createDirectory(String remoteDir) throws Exception {
        ChannelSftp channelSftp = null;
        try {
            channelSftp = jschConnectionPool.borrowObject();
            
            // 创建目录及其父目录
            try {
                channelSftp.mkdir(remoteDir);
            } catch (SftpException e) {
                if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                    // 父目录不存在情况下的处理
                    createDirectoryRecursively(channelSftp, remoteDir);
                } else {
                    throw e;
                }
            }
        } finally {
            if (channelSftp != null) {
                jschConnectionPool.returnObject(channelSftp);
            }
        }
    }
    
    /**
     * 递归创建目录
     */
    private void createDirectoryRecursively(ChannelSftp channelSftp, String remoteDir) throws SftpException {
        String[] dirs = remoteDir.split("/");
        String tempPath = "";
        
        for (String dir : dirs) {
            if (dir.isEmpty()) {
                continue;
            }
            
            tempPath += "/" + dir;
            try {
                channelSftp.cd(tempPath);
            } catch (SftpException e) {
                if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                    channelSftp.mkdir(tempPath);
                    channelSftp.cd(tempPath);
                } else {
                    throw e;
                }
            }
        }
    }
}
```

## 高级特性

### 连接池监控

```java
import io.github.archer099.sftp.core.JschConnectionPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.jcraft.jsch.ChannelSftp;

@Service
public class SftpMonitorService {

    @Autowired
    private JschConnectionPool jschConnectionPool;
    
    /**
     * 获取连接池状态信息
     */
    public String getPoolStatus(String poolKey) {
        GenericObjectPool<ChannelSftp> pool = poolKey == null ? 
                jschConnectionPool.getPool("default") : 
                jschConnectionPool.getPool(poolKey);
        
        if (pool == null) {
            return "Pool not found";
        }
        
        return String.format(
            "Pool Stats - Active: %d, Idle: %d, Waiting: %d, Created: %d, Borrowed: %d, Returned: %d, Destroyed: %d",
            pool.getNumActive(),
            pool.getNumIdle(),
            pool.getNumWaiters(),
            pool.getCreatedCount(),
            pool.getBorrowedCount(),
            pool.getReturnedCount(),
            pool.getDestroyedCount()
        );
    }
}
```

### 自定义连接验证

在某些场景下，你可能需要自定义连接验证逻辑：

```java
import com.jcraft.jsch.ChannelSftp;
import io.github.archer099.sftp.core.JschConnectionPool;
import io.github.archer099.sftp.core.JschFactory;
import io.github.archer099.sftp.core.SftpInfoProperties;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class CustomSftpPoolService {

    /**
     * 创建自定义验证逻辑的SFTP连接池
     */
    public GenericObjectPool<ChannelSftp> createCustomValidationPool(SftpInfoProperties properties) {
        // 创建连接工厂
        JschFactory jschFactory = new JschFactory(
                properties.getHost(),
                properties.getPort(),
                properties.getUsername(),
                properties.getPassword()
        );
        
        // 创建连接池配置
        GenericObjectPoolConfig<ChannelSftp> config = new GenericObjectPoolConfig<>();
        config.setMaxTotal(properties.getMaxTotal());
        config.setMinIdle(properties.getMinIdle());
        config.setMaxIdle(properties.getMaxIdle());
        config.setMaxWait(properties.getConnectTimeout());
        
        // 设置自定义验证逻辑
        config.setTestOnBorrow(true);  // 借用对象前测试
        config.setTestOnReturn(true);  // 归还对象前测试
        config.setTestWhileIdle(true); // 空闲时测试
        config.setTimeBetweenEvictionRuns(Duration.ofMinutes(1)); // 定期检查空闲连接
        
        // 创建并返回连接池
        return new GenericObjectPool<>(jschFactory, config);
    }
}
```

## 最佳实践

### 异常处理

处理 SFTP 操作中的异常：

```java
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import io.github.archer099.sftp.core.JschConnectionPool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SftpExceptionHandlingService {

    @Autowired
    private JschConnectionPool jschConnectionPool;
    
    /**
     * 安全的SFTP操作
     */
    public boolean safeOperation(String remotePath, SftpOperation operation) {
        ChannelSftp channelSftp = null;
        try {
            channelSftp = jschConnectionPool.borrowObject();
            operation.execute(channelSftp);
            return true;
        } catch (SftpException e) {
            switch (e.id) {
                case ChannelSftp.SSH_FX_NO_SUCH_FILE:
                    log.error("文件不存在: {}", remotePath, e);
                    break;
                case ChannelSftp.SSH_FX_PERMISSION_DENIED:
                    log.error("权限不足: {}", remotePath, e);
                    break;
                default:
                    log.error("SFTP操作失败: {}", remotePath, e);
            }
            return false;
        } catch (Exception e) {
            log.error("SFTP连接或操作失败", e);
            return false;
        } finally {
            if (channelSftp != null) {
                try {
                    jschConnectionPool.returnObject(channelSftp);
                } catch (Exception e) {
                    log.error("归还SFTP连接失败", e);
                }
            }
        }
    }
    
    /**
     * SFTP操作接口
     */
    @FunctionalInterface
    public interface SftpOperation {
        void execute(ChannelSftp channelSftp) throws SftpException;
    }
}
```

### 连接池配置优化

```yaml
sftp-pool:
  enable: true
  host: sftp.example.com
  port: 22
  username: ftpuser
  password: ftppassword
  # 连接超时设置为较短时间，避免长时间阻塞
  connect-timeout: 3s
  # 根据实际并发需求调整池大小
  max-total: 10
  min-idle: 2
  max-idle: 5
  # 定期清理长时间不用的连接
  min-evictable-idle-time: 5m
```

### 资源管理

使用 try-with-resources 模式简化连接管理：

```java
import com.jcraft.jsch.ChannelSftp;
import io.github.archer099.sftp.core.JschConnectionPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SftpResourceService {

    @Autowired
    private JschConnectionPool jschConnectionPool;
    
    /**
     * SFTP连接包装类，支持try-with-resources
     */
    public class SftpResource implements AutoCloseable {
        private final ChannelSftp channelSftp;
        private final String poolKey;
        
        public SftpResource(ChannelSftp channelSftp, String poolKey) {
            this.channelSftp = channelSftp;
            this.poolKey = poolKey;
        }
        
        public ChannelSftp getChannelSftp() {
            return channelSftp;
        }
        
        @Override
        public void close() {
            if (poolKey == null) {
                jschConnectionPool.returnObject(channelSftp);
            } else {
                jschConnectionPool.returnObject(poolKey, channelSftp);
            }
        }
    }
    
    /**
     * 获取SFTP资源
     */
    public SftpResource getSftpResource() throws Exception {
        return new SftpResource(jschConnectionPool.borrowObject(), null);
    }
    
    /**
     * 获取指定池的SFTP资源
     */
    public SftpResource getSftpResource(String poolKey) throws Exception {
        return new SftpResource(jschConnectionPool.borrowObject(poolKey), poolKey);
    }
    
    /**
     * 使用示例
     */
    public void uploadFile(String localPath, String remotePath) throws Exception {
        try (SftpResource resource = getSftpResource()) {
            ChannelSftp sftp = resource.getChannelSftp();
            // 上传文件...
            sftp.put(localPath, remotePath);
        }
    }
}
```

## 常见问题

### Q: 如何处理 SFTP 连接超时？
A: 可以通过设置连接超时参数来控制：

```yaml
sftp-pool:
  connect-timeout: 5s
```

同时，在代码中应该添加适当的异常处理：

```java
try {
    channelSftp = jschConnectionPool.borrowObject();
    // SFTP操作...
} catch (java.net.SocketTimeoutException e) {
    // 处理连接超时
} catch (Exception e) {
    // 处理其他异常
}
```

### Q: 如何处理大文件上传？
A: 对于大文件上传，应该使用流式处理，并考虑超时设置：

```java
public void uploadLargeFile(InputStream inputStream, String remotePath) throws Exception {
    ChannelSftp channelSftp = null;
    try {
        channelSftp = jschConnectionPool.borrowObject();
        channelSftp.put(inputStream, remotePath);
    } finally {
        if (channelSftp != null) {
            jschConnectionPool.returnObject(channelSftp);
        }
        if (inputStream != null) {
            inputStream.close();
        }
    }
}
```

### Q: 如何确保连接池正确关闭？
A: 在应用关闭时，应确保连接池正确关闭：

```java
import io.github.archer099.sftp.core.JschConnectionPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

@Component
public class SftpPoolShutdownManager {

    @Autowired
    private JschConnectionPool jschConnectionPool;
    
    @PreDestroy
    public void shutdown() {
        try {
            jschConnectionPool.close();
        } catch (Exception e) {
            // 记录关闭错误
        }
    }
}
```

## 许可证

本项目遵循 [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0) 开源许可证。

## 贡献指南

欢迎提交问题和贡献代码，请通过 GitHub Issue 和 Pull Request 参与项目开发。 