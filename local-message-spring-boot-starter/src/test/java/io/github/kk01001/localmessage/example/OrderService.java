package io.github.kk01001.localmessage.example;

import io.github.kk01001.localmessage.service.LocalMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 订单服务示例
 * 演示如何在业务代码中使用本地消息表
 *
 * @author kk01001
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final LocalMessageService localMessageService;
    
    /**
     * 创建订单
     * 在同一个事务中保存订单数据和发送本地消息
     */
    @Transactional(rollbackFor = Exception.class)
    public void createOrder(String orderId, String orderData) {
        try {
            // 1. 保存订单数据到数据库
            saveOrderToDatabase(orderId, orderData);
            
            // 2. 发送本地消息（在同一个事务中）
            String messageContent = String.format("{\"orderId\":\"%s\",\"action\":\"created\",\"data\":%s}", 
                    orderId, orderData);
            
            Long messageId = localMessageService.sendMessage(
                    "ORDER",           // 业务类型
                    orderId,           // 业务ID
                    messageContent,    // 消息内容
                    3,                 // 最大重试次数
                    null               // 扩展数据
            );
            
            log.info("订单创建成功，本地消息已发送: orderId={}, messageId={}", orderId, messageId);
            
        } catch (Exception e) {
            log.error("创建订单失败: orderId={}", orderId, e);
            throw e; // 抛出异常，触发事务回滚
        }
    }
    
    /**
     * 更新订单状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateOrderStatus(String orderId, String status) {
        try {
            // 1. 更新订单状态
            updateOrderStatusInDatabase(orderId, status);
            
            // 2. 发送状态变更消息
            String messageContent = String.format("{\"orderId\":\"%s\",\"action\":\"statusChanged\",\"status\":\"%s\"}", 
                    orderId, status);
            
            Long messageId = localMessageService.sendMessage(
                    "ORDER",           // 业务类型
                    orderId,           // 业务ID
                    messageContent,    // 消息内容
                    5,                 // 最大重试次数
                    null               // 扩展数据
            );
            
            log.info("订单状态更新成功，本地消息已发送: orderId={}, status={}, messageId={}", 
                    orderId, status, messageId);
            
        } catch (Exception e) {
            log.error("更新订单状态失败: orderId={}, status={}", orderId, status, e);
            throw e;
        }
    }
    
    /**
     * 模拟保存订单到数据库
     */
    private void saveOrderToDatabase(String orderId, String orderData) {
        // 实际实现中，这里会调用DAO层保存订单数据
        log.info("保存订单到数据库: orderId={}, data={}", orderId, orderData);
        
        // 模拟可能的数据库异常
        if (orderId.contains("error")) {
            throw new RuntimeException("数据库保存失败");
        }
    }
    
    /**
     * 模拟更新订单状态到数据库
     */
    private void updateOrderStatusInDatabase(String orderId, String status) {
        // 实际实现中，这里会调用DAO层更新订单状态
        log.info("更新订单状态到数据库: orderId={}, status={}", orderId, status);
        
        // 模拟可能的数据库异常
        if (status.equals("ERROR")) {
            throw new RuntimeException("数据库更新失败");
        }
    }
}
