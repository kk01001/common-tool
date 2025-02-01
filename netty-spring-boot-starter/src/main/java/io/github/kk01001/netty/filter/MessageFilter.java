package io.github.kk01001.netty.filter;

import io.github.kk01001.netty.session.WebSocketSession;

/**
 * 消息过滤器接口
 */
public interface MessageFilter {
    
    /**
     * 过滤消息
     * @param session 会话
     * @param message 消息内容
     * @return true表示通过，false表示拦截
     */
    boolean doFilter(WebSocketSession session, String message);
    
    /**
     * 获取过滤器优先级，数字越小优先级越高
     */
    default int getOrder() {
        return 0;
    }
} 