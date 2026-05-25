package io.github.archer099.redisson.example.controller;

import io.github.archer099.redisson.template.MultiRedissonTemplate;
import org.redisson.api.RLock;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author archer099
 * @date 2026-01-17 10:00:00
 * @description 秒杀/库存扣减 Demo - 分布式锁实战案例
 */
@RestController
@RequestMapping("/api/seckill")
public class SeckillController {

    private static final String STOCK_KEY = "seckill:stock:";
    private static final String ORDER_KEY = "seckill:orders:";
    private static final String LOCK_KEY = "seckill:lock:";

    private final MultiRedissonTemplate redissonTemplate;

    public SeckillController(MultiRedissonTemplate redissonTemplate) {
        this.redissonTemplate = redissonTemplate;
    }

    /**
     * 初始化商品库存
     */
    @PostMapping("/init")
    public Map<String, Object> initStock(@RequestParam(defaultValue = "product001") String productId,
                                         @RequestParam(defaultValue = "100") int stock) {
        Map<String, Object> result = new LinkedHashMap<>();

        redissonTemplate.setAtomicLong(STOCK_KEY + productId, stock);
        redissonTemplate.delete(ORDER_KEY + productId);

        result.put("success", true);
        result.put("productId", productId);
        result.put("stock", stock);
        result.put("message", "库存初始化成功");
        return result;
    }

    /**
     * 查询库存
     */
    @GetMapping("/stock")
    public Map<String, Object> getStock(@RequestParam(defaultValue = "product001") String productId) {
        Map<String, Object> result = new LinkedHashMap<>();

        long stock = redissonTemplate.getAtomicLong(STOCK_KEY + productId);
        List<String> orders = redissonTemplate.lrange(ORDER_KEY + productId, 0, -1);

        result.put("productId", productId);
        result.put("stock", stock);
        result.put("soldCount", orders != null ? orders.size() : 0);
        result.put("orders", orders != null ? orders : new ArrayList<>());
        return result;
    }

    /**
     * 秒杀下单（使用分布式锁）
     */
    @PostMapping("/order")
    public Map<String, Object> seckill(@RequestParam(defaultValue = "product001") String productId,
                                       @RequestParam String userId) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("productId", productId);
        result.put("userId", userId);

        String lockKey = LOCK_KEY + productId;
        RLock lock = redissonTemplate.getLock(lockKey);

        try {
            // 尝试获取锁，等待3秒，锁定10秒
            boolean acquired = lock.tryLock(3, 10, TimeUnit.SECONDS);

            if (!acquired) {
                result.put("success", false);
                result.put("message", "系统繁忙，请稍后重试");
                return result;
            }

            // 检查库存
            long stock = redissonTemplate.getAtomicLong(STOCK_KEY + productId);
            if (stock <= 0) {
                result.put("success", false);
                result.put("message", "商品已售罄");
                return result;
            }

            // 扣减库存
            long newStock = redissonTemplate.decrement(STOCK_KEY + productId);

            // 生成订单
            String orderId = "ORDER_" + System.currentTimeMillis() + "_" + userId;
            String orderInfo = String.format("%s|%s|%s",
                    orderId, userId,
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            redissonTemplate.rpush(ORDER_KEY + productId, orderInfo);

            result.put("success", true);
            result.put("orderId", orderId);
            result.put("remainStock", newStock);
            result.put("message", "秒杀成功！");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            result.put("success", false);
            result.put("message", "操作被中断");
        } finally {
            // 释放锁（只有持有锁的线程才能释放）
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

        return result;
    }

    /**
     * 秒杀下单（不使用锁 - 演示并发问题）
     */
    @PostMapping("/order-no-lock")
    public Map<String, Object> seckillNoLock(@RequestParam(defaultValue = "product001") String productId,
                                             @RequestParam String userId) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("productId", productId);
        result.put("userId", userId);

        // 检查库存（存在并发问题！）
        long stock = redissonTemplate.getAtomicLong(STOCK_KEY + productId);
        if (stock <= 0) {
            result.put("success", false);
            result.put("message", "商品已售罄");
            return result;
        }

        // 扣减库存
        long newStock = redissonTemplate.decrement(STOCK_KEY + productId);

        // 检查是否超卖
        if (newStock < 0) {
            // 恢复库存
            redissonTemplate.increment(STOCK_KEY + productId);
            result.put("success", false);
            result.put("message", "商品已售罄（超卖回滚）");
            return result;
        }

        // 生成订单
        String orderId = "ORDER_" + System.currentTimeMillis() + "_" + userId;
        String orderInfo = String.format("%s|%s|%s",
                orderId, userId,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        redissonTemplate.rpush(ORDER_KEY + productId, orderInfo);

        result.put("success", true);
        result.put("orderId", orderId);
        result.put("remainStock", newStock);
        result.put("message", "秒杀成功（无锁）！");

        return result;
    }

    /**
     * 读写锁示例 - 获取库存（使用读锁）
     */
    @GetMapping("/stock-rwlock")
    public Map<String, Object> getStockWithReadLock(@RequestParam(defaultValue = "product001") String productId) {
        Map<String, Object> result = new LinkedHashMap<>();

        String lockKey = LOCK_KEY + "rw:" + productId;
        RLock readLock = redissonTemplate.getReadWriteLock(lockKey).readLock();

        try {
            readLock.lock(5, TimeUnit.SECONDS);

            long stock = redissonTemplate.getAtomicLong(STOCK_KEY + productId);
            List<String> orders = redissonTemplate.lrange(ORDER_KEY + productId, 0, -1);

            result.put("productId", productId);
            result.put("stock", stock);
            result.put("soldCount", orders != null ? orders.size() : 0);
            result.put("lockType", "READ_LOCK");

        } finally {
            if (readLock.isHeldByCurrentThread()) {
                readLock.unlock();
            }
        }

        return result;
    }
}
