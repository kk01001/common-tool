# common-spring-boot-starter
SpringBoot Common Tool Starter
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
