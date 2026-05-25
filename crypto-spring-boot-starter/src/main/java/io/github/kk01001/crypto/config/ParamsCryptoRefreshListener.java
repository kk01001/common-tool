package io.github.archer099.crypto.config;

import io.github.archer099.crypto.ParamsCryptoProvider;
import io.github.archer099.crypto.enums.CryptoType;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ParamsCryptoRefreshListener {

    private final ParamsCryptoProvider paramsCryptoProvider;

    private final ParamsCryptoProperties properties;

    @EventListener(RefreshScopeRefreshedEvent.class)
    public void onRefresh() {
        CryptoType type = properties.getType();
        switch (type) {
            case RSA, SM2 -> paramsCryptoProvider.refreshRSAKey(properties.getPublicKey(), properties.getPrivateKey());
            case AES, SM4 -> paramsCryptoProvider.refreshKey(properties.getKey());
        }
    }
} 