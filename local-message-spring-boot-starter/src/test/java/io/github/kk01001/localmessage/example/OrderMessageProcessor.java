package io.github.kk01001.localmessage.example;

import io.github.kk01001.localmessage.entity.LocalMessage;
import io.github.kk01001.localmessage.processor.MessageProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 订单消息处理器示例
 * 演示如何实现MessageProcessor接口
 *
 * @author kk01001
 */
@Slf4j
@Component
public class OrderMessageProcessor implements MessageProcessor {
    
    @Override
    public String getBusinessType() {
        return "ORDER";
    }
    
    @Override
    public ProcessResult process(LocalMessage message) {
        try {
            log.info("开始处理订单消息: messageId={}, businessId={}, content={}", 
                    message.getId(), message.getBusinessId(), message.getMessageContent());
            
            // 模拟调用外部接口
            boolean success = callExternalApi(message);
            
            if (success) {
                log.info("订单消息处理成功: messageId={}", message.getId());
                return ProcessResult.success();
            } else {
                log.warn("订单消息处理失败，需要重试: messageId={}", message.getId());
                return ProcessResult.failureWithRetry("外部接口调用失败", 60L); // 60秒后重试
            }
        } catch (Exception e) {
            log.error("订单消息处理异常: messageId={}", message.getId(), e);
            return ProcessResult.failureWithRetry("处理异常: " + e.getMessage());
        }
    }
    
    /**
     * 模拟调用外部接口
     */
    private boolean callExternalApi(LocalMessage message) {
        // 这里模拟外部接口调用
        // 实际使用时，可以调用HTTP接口、发送MQ消息等
        
        // 模拟90%的成功率
        return Math.random() > 0.1;
    }
}
