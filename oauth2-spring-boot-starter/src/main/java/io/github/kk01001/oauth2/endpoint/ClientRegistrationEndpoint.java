package io.github.kk01001.oauth2.endpoint;

import io.github.kk01001.oauth2.model.ClientRegistrationDto;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * @author kk01001
 * @date 2025-02-22 14:31:00
 * @description 客户端注册端点
 */
@RestController
@RequestMapping("/oauth2/client")
public class ClientRegistrationEndpoint {

    private final RegisteredClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    public ClientRegistrationEndpoint(RegisteredClientRepository clientRepository,
                                    PasswordEncoder passwordEncoder) {
        this.clientRepository = clientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 注册新客户端
     */
    @PostMapping("/register")
    public ClientRegistrationDto registerClient(@RequestBody ClientRegistrationDto dto) {
        RegisteredClient registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
            .clientId(dto.getClientId())
            .clientSecret(passwordEncoder.encode(dto.getClientSecret()))
            .clientAuthenticationMethods(methods -> methods.addAll(dto.getClientAuthenticationMethods()))
            .authorizationGrantTypes(types -> types.addAll(dto.getAuthorizationGrantTypes()))
            .redirectUris(uris -> uris.addAll(dto.getRedirectUris()))
            .scopes(scopes -> scopes.addAll(dto.getScopes()))
            .build();

        clientRepository.save(registeredClient);
        return dto;
    }

    /**
     * 删除客户端
     */
    @DeleteMapping("/{clientId}")
    public void removeClient(@PathVariable String clientId) {
        RegisteredClient client = clientRepository.findByClientId(clientId);
        if (client != null) {
            // 这里需要自定义实现删除方法，因为默认的InMemoryRegisteredClientRepository不支持删除
            // 实际项目中应该使用JdbcRegisteredClientRepository
        }
    }
} 