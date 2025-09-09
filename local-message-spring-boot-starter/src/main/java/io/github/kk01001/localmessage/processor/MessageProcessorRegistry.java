package io.github.kk01001.localmessage.processor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 消息处理器注册中心
 * 管理所有的消息处理器
 *
 * @author kk01001
 */
@Slf4j
@Component
public class MessageProcessorRegistry implements InitializingBean {
    
    private final Map<String, MessageProcessor> processorMap = new ConcurrentHashMap<>();
    
    private final List<MessageProcessor> messageProcessors;
    
    public MessageProcessorRegistry(List<MessageProcessor> messageProcessors) {
        this.messageProcessors = messageProcessors;
    }
    
    @Override
    public void afterPropertiesSet() {
        for (MessageProcessor processor : messageProcessors) {
            String businessType = processor.getBusinessType();
            if (processorMap.containsKey(businessType)) {
                throw new IllegalStateException("重复的业务类型处理器: " + businessType);
            }
            processorMap.put(businessType, processor);
            log.info("注册消息处理器: businessType={}, processor={}", businessType, processor.getClass().getSimpleName());
        }
    }
    
    /**
     * 根据业务类型获取处理器
     *
     * @param businessType 业务类型
     * @return 消息处理器
     */
    public MessageProcessor getProcessor(String businessType) {
        MessageProcessor processor = processorMap.get(businessType);
        if (processor == null) {
            throw new IllegalArgumentException("未找到业务类型对应的处理器: " + businessType);
        }
        return processor;
    }
    
    /**
     * 检查是否存在指定业务类型���处理器
     *
     * @param businessType 业务类型
     * @return 是否存在
     */
    public boolean hasProcessor(String businessType) {
        return processorMap.containsKey(businessType);
    }
}
