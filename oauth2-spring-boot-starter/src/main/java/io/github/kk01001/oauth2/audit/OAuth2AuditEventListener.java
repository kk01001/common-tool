package io.github.kk01001.oauth2.audit;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2TokenRevocationAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * @author kk01001
 * @date 2025-02-22 14:31:00
 * @description OAuth2审计事件监听器
 */
@Component
public class OAuth2AuditEventListener {

    /**
     * 认证成功事件
     */
    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) {
        // 记录认证成功日志
        System.out.println("Authentication success: " + event.getAuthentication().getName());
    }

    /**
     * 认证失败事件
     */
    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent event) {
        // 记录认证失败日志
        System.out.println("Authentication failed: " + event.getException().getMessage());
    }

    /**
     * 令牌撤销事件
     */
    @EventListener
    public void onTokenRevocation(OAuth2TokenRevocationAuthenticationToken event) {
        // 记录令牌撤销日志
        System.out.println("Token revoked: " + event.getToken());
    }
} 