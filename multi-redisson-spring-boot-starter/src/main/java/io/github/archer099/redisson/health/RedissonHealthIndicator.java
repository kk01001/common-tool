package io.github.archer099.redisson.health;

import io.github.archer099.redisson.holder.RedissonClientHolder;
import org.redisson.api.RedissonClient;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author archer099
 * @date 2026-01-15 17:00:00
 * @description Redisson 健康检查指示器
 */
public class RedissonHealthIndicator extends AbstractHealthIndicator {

    private final RedissonClientHolder clientHolder;

    public RedissonHealthIndicator(RedissonClientHolder clientHolder) {
        super("Redisson health check failed");
        this.clientHolder = clientHolder;
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
     * 健康检查固定 Key（只读，不产生脏数据）
     */
    private static final String HEALTH_CHECK_KEY = "redisson:health:ping";

    /**
     * 检查单个客户端健康状态（使用只读操作，不产生脏数据）
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

            client.getBucket(HEALTH_CHECK_KEY).isExists();
            long responseTime = System.currentTimeMillis() - startTime;
            return new ClientHealth(true, responseTime, null);

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
