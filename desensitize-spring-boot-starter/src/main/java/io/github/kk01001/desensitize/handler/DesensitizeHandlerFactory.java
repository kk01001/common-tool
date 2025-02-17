package io.github.kk01001.desensitize.handler;

import io.github.kk01001.desensitize.annotation.DesensitizeFor;
import io.github.kk01001.desensitize.enums.DesensitizeType;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;

/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description
 */
public class DesensitizeHandlerFactory implements InitializingBean {
    
    private final Map<DesensitizeType, DesensitizeHandler> handlers = new HashMap<>();
    private final ApplicationContext applicationContext;
    
    public DesensitizeHandlerFactory(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
    
    @Override
    public void afterPropertiesSet() {
        // 注册默认处理器
        registerDefaultHandlers();
        
        // 注册自定义处理器
        registerCustomHandlers();
    }
    
    private void registerDefaultHandlers() {
        handlers.put(DesensitizeType.PHONE, new PhoneDesensitizeHandler());
        handlers.put(DesensitizeType.EMAIL, new EmailDesensitizeHandler());
        handlers.put(DesensitizeType.NAME, new NameDesensitizeHandler());
        handlers.put(DesensitizeType.ID_CARD, new IdCardDesensitizeHandler());
        handlers.put(DesensitizeType.BANK_CARD, new BankCardDesensitizeHandler());
        handlers.put(DesensitizeType.ADDRESS, new AddressDesensitizeHandler());
        handlers.put(DesensitizeType.PASSWORD, new PasswordDesensitizeHandler());
        handlers.put(DesensitizeType.CAR_NUMBER, new CarNumberDesensitizeHandler());
        handlers.put(DesensitizeType.FIXED_PHONE, new FixedPhoneDesensitizeHandler());
        handlers.put(DesensitizeType.IPV4, new IpDesensitizeHandler());
        handlers.put(DesensitizeType.PASSPORT, new PassportDesensitizeHandler());
        handlers.put(DesensitizeType.MASK_ALL, new MastAllDesensitizeHandler());
        handlers.put(DesensitizeType.DOMAIN, new DomainDesensitizeHandler());
    }
    
    private void registerCustomHandlers() {
        applicationContext.getBeansOfType(DesensitizeHandler.class)
                .forEach((name, handler) -> {
                    DesensitizeFor annotation = handler.getClass().getAnnotation(DesensitizeFor.class);
                    if (annotation != null) {
                        handlers.put(annotation.value(), handler);
                    }
                });
    }
    
    public DesensitizeHandler getHandler(DesensitizeType type) {
        return handlers.get(type);
    }
    
    public void registerHandler(DesensitizeType type, DesensitizeHandler handler) {
        handlers.put(type, handler);
    }
} 