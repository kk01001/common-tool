# common-spring-boot-starter 基于SpringBoot3.x JDk21

> SpringBoot Common Tool Starter

## XXL-JOB

```yaml
xxl-job:
  accessToken:
  adminAddresses: http://127.0.0.1:38080/xxl-job-admin
  logRetentionDays: 30
  logPath: /home/logs/${spring.application.name}
  address:
  ip:
  port: 28001
  appName: ${spring.application.name}
  title: ${spring.application.name}
  userName: admin
  password: xxx
  enable: true
```

## multi redis

```yaml
spring:
  data:
    redis:
    password: 234
    masterConnectionPoolSize: 200
    cluster:
      nodes:
        - 127.0.0.1:9100
      max-redirects: 5
      location: A
    cluster2:
      # 激活异地机房
      active: false
      nodes:
        - 127.0.0.1:9100
      max-redirects: 5
      location: B
    timeout: 10000
    database: 0
    lettuce:
      pool:
        maxIdle: 8
        minIdle: 0
        maxActive: 8
        timeBetweenEvictionRuns: 600s
```

## Mybatis Plus

### yaml配置

```yaml
mybatis:
  dynamic:
    table-rule:
      # 逻辑表名(不带后缀) : 真实表名模板
      logic_table_name: logic_table_name_{company_code}_{month}

spring:
  jackson:
    default-property-inclusion: non_null
  datasource:
    dynamic:
      primary: ms
      strict: true
      datasource:
        cdr:
          url: jdbc:mysql://172.16.250.234:3300/cloudcc_cdr?characterEncoding=UTF-8&useUnicode=true&useSSL=false&tinyInt1isBit=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai&rewriteBatchedStatements=true
          username: root
          password: 123456
          type: com.zaxxer.hikari.HikariDataSource
        ms:
          url: jdbc:mysql://172.16.250.234:3300/cloudcc_ms?characterEncoding=UTF-8&useUnicode=true&useSSL=false&tinyInt1isBit=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai&rewriteBatchedStatements=true
          username: root
          password: 123456
          type: com.zaxxer.hikari.HikariDataSource
      hikari:
        min-idle: 10
        max-pool-size: 100
        idle-timeout: 60000
        max-lifetime: 600000
        connection-timeout: 3000
        connection-test-query: SELECT 1 FROM DUAL
        validation-timeout: 10000
```

### 动态表名操作数据库demo

```java
// 写在mapper接口上
@DS("cdr")  
```

```java
public class Demo {
    public void test() {
        RequestDataHelper.setRequestData(CommonConstant.COMPANY_CODE_KEY, extStatusLog.getCompanyCode());
        RequestDataHelper.setRequestData(CommonConstant.MONTH_KEY, month);
        try {
            // mapper接口执行

        } catch (Exception e) {
            log.error("db异常: ", e);
        } finally {
            RequestDataHelper.remove();
        }
    }
}
```
