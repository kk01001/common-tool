package io.github.kk01001.redisson.health;

import io.github.kk01001.redisson.holder.RedissonClientHolder;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author kk01001
 * @date 2026-01-15 17:00:00
 * @description Redisson 健康检查指示器
 */
public class RedissonHealthIndicator extends AbstractHealthIndicator {

    private final RedissonClientHolder clientHolder;

    /**
     * 健康检查超时时间
     */
    private final Duration timeout;

    /**
     * 健康检查测试 Key 前缀
     */
    private static final String HEALTH_CHECK_KEY_PREFIX = "redisson:health:check:";

    public RedissonHealthIndicator(RedissonClientHolder clientHolder) {
        this(clientHolder, Duration.ofSeconds(3));
    }

    public RedissonHealthIndicator(RedissonClientHolder clientHolder, Duration timeout) {
        super("Redisson health check failed");
        this.clientHolder = clientHolder;
        this.timeout = timeout;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        Map<String, Object> details = new LinkedHashMap<>();
        Map<String, ClientHealth> clientHealthMap = new LinkedHashMap<>();

        boolean allHealthy = true;
        int totalClients = 0;
        int healthyClients = 0;

        for (String clientName : clientHolder.getClientNames()) {
            totalClients++;
            ClientHealth health = checkClientHealth(clientName);
            clientHealthMap.put(clientName, health);

            if (health.isHealthy()) {
                healthyClients++;
            } else {
                allHealthy = false;
            }
        }

        // 构建详情
        details.put("totalClients", totalClients);
        details.put("healthyClients", healthyClients);
        details.put("primaryClient", clientHolder.getPrimaryName());

        Map<String, Object> clientsDetail = new LinkedHashMap<>();
        for (Map.Entry<String, ClientHealth> entry : clientHealthMap.entrySet()) {
            Map<String, Object> clientDetail = new LinkedHashMap<>();
            ClientHealth health = entry.getValue();
            clientDetail.put("status", health.isHealthy() ? "UP" : "DOWN");
            clientDetail.put("responseTimeMs", health.getResponseTimeMs());
            if (health.getError() != null) {
                clientDetail.put("error", health.getError());
            }
            clientsDetail.put(entry.getKey(), clientDetail);
        }
        details.put("clients", clientsDetail);

        if (allHealthy) {
            builder.up().withDetails(details);
        } else if (healthyClients > 0) {
            // 部分健康
            builder.status(new Status("DEGRADED", "Some Redis clients are unhealthy"))
                    .withDetails(details);
        } else {
            builder.down().withDetails(details);
        }
    }

    /**
     * 检查单个客户端健康状态
     */
    private ClientHealth checkClientHealth(String clientName) {
        long startTime = System.currentTimeMillis();
        try {
            RedissonClient client = clientHolder.getClient(clientName);
            if (client == null) {
                return new ClientHealth(false, 0, "Client not found");
            }

            if (client.isShutdown()) {
                return new ClientHealth(false, 0, "Client is shutdown");
            }

            // 执行 PING 测试
            String testKey = HEALTH_CHECK_KEY_PREFIX + UUID.randomUUID().toString();
            String testValue = "health-check-" + System.currentTimeMillis();

            RBucket<String> bucket = client.getBucket(testKey);
            bucket.set(testValue, Duration.ofSeconds(10));
            String result = bucket.get();
            bucket.delete();

            long responseTime = System.currentTimeMillis() - startTime;

            if (testValue.equals(result)) {
                return new ClientHealth(true, responseTime, null);
            } else {
                return new ClientHealth(false, responseTime, "Value mismatch");
            }

        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            return new ClientHealth(false, responseTime, e.getMessage());
        }
    }

    /**
     * 客户端健康状态
     */
    private static class ClientHealth {
        private final boolean healthy;
        private final long responseTimeMs;
        private final String error;

        public ClientHealth(boolean healthy, long responseTimeMs, String error) {
            this.healthy = healthy;
            this.responseTimeMs = responseTimeMs;
            this.error = error;
        }

        public boolean isHealthy() {
            return healthy;
        }

        public long getResponseTimeMs() {
            return responseTimeMs;
        }

        public String getError() {
            return error;
        }
    }
}
