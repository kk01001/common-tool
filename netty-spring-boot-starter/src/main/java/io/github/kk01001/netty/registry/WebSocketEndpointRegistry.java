package io.github.kk01001.netty.registry;

import io.github.kk01001.netty.annotation.OnBinaryMessage;
import io.github.kk01001.netty.annotation.OnClose;
import io.github.kk01001.netty.annotation.OnError;
import io.github.kk01001.netty.annotation.OnMessage;
import io.github.kk01001.netty.annotation.OnOpen;
import io.github.kk01001.netty.annotation.WebSocketEndpoint;
import io.github.kk01001.netty.config.NettyWebSocketProperties;
import io.github.kk01001.netty.session.WebSocketSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author kk01001
 * @date 2026-03-07 10:00:00
 * @description WebSocket 端点注册表，支持多路径注册
 */
@Slf4j
public class WebSocketEndpointRegistry implements SmartInitializingSingleton {

    private final ApplicationContext applicationContext;
    private final NettyWebSocketProperties properties;
    private final Map<String, EndpointMethodHandler> pathHandlers = new ConcurrentHashMap<>();

    public WebSocketEndpointRegistry(ApplicationContext applicationContext, NettyWebSocketProperties properties) {
        this.applicationContext = applicationContext;
        this.properties = properties;
    }

    @Override
    public void afterSingletonsInstantiated() {
        scanEndpoints();
    }

    /**
     * 获取所有已注册的路径
     */
    public Set<String> getRegisteredPaths() {
        return pathHandlers.keySet();
    }

    /**
     * 判断是否存在指定路径的端点
     */
    public boolean hasEndpoint(String path) {
        return pathHandlers.containsKey(path);
    }

    private void scanEndpoints() {
        String[] beanNames = applicationContext.getBeanNamesForType(Object.class);
        for (String beanName : beanNames) {
            Object bean = applicationContext.getBean(beanName);
            Class<?> beanType = bean.getClass();

            WebSocketEndpoint endpoint = AnnotatedElementUtils.findMergedAnnotation(
                    beanType, WebSocketEndpoint.class);
            if (endpoint != null) {
                registerEndpoint(endpoint, bean, beanType);
            }
        }
    }

    private void registerEndpoint(WebSocketEndpoint endpoint, Object bean, Class<?> beanType) {
        String path = StringUtils.hasText(endpoint.value()) ? endpoint.value() : properties.getPath();
        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        EndpointMethodHandler handler = new EndpointMethodHandler(bean);

        ReflectionUtils.doWithMethods(beanType, method -> {
            if (method.isAnnotationPresent(OnOpen.class)) {
                handler.setOnOpenMethod(method);
            } else if (method.isAnnotationPresent(OnMessage.class)) {
                handler.setOnMessageMethod(method);
            } else if (method.isAnnotationPresent(OnBinaryMessage.class)) {
                handler.setOnBinaryMessageMethod(method);
            } else if (method.isAnnotationPresent(OnClose.class)) {
                handler.setOnCloseMethod(method);
            } else if (method.isAnnotationPresent(OnError.class)) {
                handler.setOnErrorMethod(method);
            }
        });

        pathHandlers.put(path, handler);
        log.info("注册WebSocket端点: path={}, bean={}", path, beanType.getName());
    }

    /**
     * 根据 session 的 path 获取对应的 handler
     */
    private EndpointMethodHandler getHandler(WebSocketSession session) {
        if (session == null || session.getPath() == null) {
            return null;
        }
        return pathHandlers.get(session.getPath());
    }

    public void handleOpen(WebSocketSession session) {
        EndpointMethodHandler handler = getHandler(session);
        if (handler != null && handler.getOnOpenMethod() != null) {
            try {
                handler.getOnOpenMethod().invoke(handler.getBean(), session);
            } catch (Exception e) {
                log.error("处理连接打开失败: path={}, sessionId={}",
                        session.getPath(), session.getId(), e);
            }
        }
    }

    public void handleMessage(WebSocketSession session, String message) {
        EndpointMethodHandler handler = getHandler(session);
        if (handler != null && handler.getOnMessageMethod() != null) {
            try {
                handler.getOnMessageMethod().invoke(handler.getBean(), session, message);
            } catch (Exception e) {
                log.error("处理消息失败: path={}, sessionId={}",
                        session.getPath(), session.getId(), e);
            }
        }
    }

    public void handleBinaryMessage(WebSocketSession session, byte[] bytes) {
        EndpointMethodHandler handler = getHandler(session);
        if (handler != null && handler.getOnBinaryMessageMethod() != null) {
            try {
                handler.getOnBinaryMessageMethod().invoke(handler.getBean(), session, bytes);
            } catch (Exception e) {
                log.error("处理二进制消息失败: path={}, sessionId={}",
                        session.getPath(), session.getId(), e);
            }
        }
    }

    public void handleClose(WebSocketSession session) {
        EndpointMethodHandler handler = getHandler(session);
        if (handler != null && handler.getOnCloseMethod() != null) {
            try {
                handler.getOnCloseMethod().invoke(handler.getBean(), session);
            } catch (Exception e) {
                log.error("处理连接关闭失败: path={}, sessionId={}",
                        session.getPath(), session.getId(), e);
            }
        }
    }

    public void handleError(WebSocketSession session, Throwable error) {
        EndpointMethodHandler handler = getHandler(session);
        if (handler != null && handler.getOnErrorMethod() != null) {
            try {
                handler.getOnErrorMethod().invoke(handler.getBean(), session, error);
            } catch (Exception e) {
                log.error("处理错误失败: path={}, sessionId={}",
                        session.getPath(), session.getId(), e);
            }
        }
    }
}
