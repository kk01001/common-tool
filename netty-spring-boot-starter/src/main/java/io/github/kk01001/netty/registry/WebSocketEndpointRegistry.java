package io.github.kk01001.netty.registry;

import io.github.kk01001.netty.annotation.*;
import io.github.kk01001.netty.session.WebSocketSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ReflectionUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class WebSocketEndpointRegistry implements SmartInitializingSingleton {
    
    private final ApplicationContext applicationContext;
    private final Map<String, EndpointMethodHandler> pathHandlers = new ConcurrentHashMap<>();
    
    public WebSocketEndpointRegistry(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
    
    @Override
    public void afterSingletonsInstantiated() {
        scanEndpoints();
    }
    
    /**
     * 扫描WebSocket端点
     */
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
    
    /**
     * 注册端点
     */
    private void registerEndpoint(WebSocketEndpoint endpoint, Object bean, Class<?> beanType) {
        String path = endpoint.path();
        EndpointMethodHandler handler = new EndpointMethodHandler(bean);
        
        // 扫描处理方法
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
     * 处理连接打开
     */
    public void handleOpen(WebSocketSession session) {
        EndpointMethodHandler handler = pathHandlers.get(session.getPath());
        if (handler != null && handler.getOnOpenMethod() != null) {
            try {
                handler.getOnOpenMethod().invoke(handler.getBean(), session);
            } catch (Exception e) {
                log.error("处理连接打开失败: path={}, sessionId={}", 
                        session.getPath(), session.getId(), e);
            }
        }
    }
    
    /**
     * 处理文本消息
     */
    public void handleMessage(WebSocketSession session, String message) {
        EndpointMethodHandler handler = pathHandlers.get(session.getPath());
        if (handler != null && handler.getOnMessageMethod() != null) {
            try {
                handler.getOnMessageMethod().invoke(handler.getBean(), session, message);
            } catch (Exception e) {
                log.error("处理消息失败: path={}, sessionId={}", 
                        session.getPath(), session.getId(), e);
            }
        }
    }

    /**
     * 处理二进制消息
     */
    public void handleBinaryMessage(WebSocketSession session, byte[] bytes) {
        EndpointMethodHandler handler = pathHandlers.get(session.getPath());
        if (handler != null && handler.getOnMessageMethod() != null) {
            try {
                handler.getOnBinaryMessageMethod().invoke(handler.getBean(), session, bytes);
            } catch (Exception e) {
                log.error("处理消息失败: path={}, sessionId={}",
                        session.getPath(), session.getId(), e);
            }
        }
    }
    
    /**
     * 处理连接关闭
     */
    public void handleClose(WebSocketSession session) {
        EndpointMethodHandler handler = pathHandlers.get(session.getPath());
        if (handler != null && handler.getOnCloseMethod() != null) {
            try {
                handler.getOnCloseMethod().invoke(handler.getBean(), session);
            } catch (Exception e) {
                log.error("处理连接关闭失败: path={}, sessionId={}", 
                        session.getPath(), session.getId(), e);
            }
        }
    }
    
    /**
     * 处理错误
     */
    public void handleError(WebSocketSession session, Throwable error) {
        EndpointMethodHandler handler = pathHandlers.get(session.getPath());
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