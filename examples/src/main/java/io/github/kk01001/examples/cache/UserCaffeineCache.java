package io.github.kk01001.examples.cache;

import com.github.benmanes.caffeine.cache.RemovalCause;
import io.github.kk01001.cache.core.AbstractLocalCaffeineCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * @author linshiqiang
 * @date 2025-03-24 14:49
 * @description
 */
@Slf4j
@Component
public class UserCaffeineCache extends AbstractLocalCaffeineCache<String, String> {

    @Override
    protected Duration getExpireAfterAccess() {
        return Duration.ofSeconds(10);
    }

    @Override
    protected long getMaximumSize() {
        return 10;
    }

    @Override
    protected int getInitialCapacity() {
        return 1;
    }

    @Override
    protected void onRemoval(String key, String value, RemovalCause cause) {
        log.info("key: {}, value: {}, cause: {}", key, value, cause);
    }

}
