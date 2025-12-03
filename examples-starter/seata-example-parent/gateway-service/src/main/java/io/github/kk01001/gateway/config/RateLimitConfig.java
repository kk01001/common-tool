package io.github.kk01001.gateway.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.caffeine.CaffeineProxyManager;
import io.github.bucket4j.distributed.proxy.AsyncProxyManager;
import io.github.bucket4j.distributed.remote.RemoteBucketState;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * @author kk01001
 * @date 2025-01-08 17:30:00
 * @description 限流配置
 */
@Configuration
public class RateLimitConfig {

    /**
     * 使用 Caffeine 缓存作为 Bucket4j 的后端
     */
    @Bean
    public AsyncProxyManager<String> caffeineProxyManager() {
        Caffeine<String, RemoteBucketState> builder = (Caffeine) Caffeine.newBuilder().maximumSize(100);
        return new CaffeineProxyManager<>(builder, Duration.ofMinutes(1)).asAsync();
    }

    /**
     * 限流 Key 解析器：根据 userId 进行限流
     */
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String userId = exchange.getRequest().getQueryParams().getFirst("userId");
            // 如果没有 userId，使用 IP 地址
            if (userId == null) {
                userId = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
            }
            return Mono.just(userId);
        };
    }
}
