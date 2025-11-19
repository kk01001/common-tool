package io.github.kk01001.threadpool.queue;

import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 可调整容量的阻塞队列
 * 基于 LinkedBlockingQueue 实现，支持动态修改容量
 * 
 * @author kk01001
 */
public class ResizableLinkedBlockingQueue<E> extends LinkedBlockingQueue<E> {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 当前容量（可变）
     */
    private volatile int capacity;
    
    /**
     * 构造函数
     * @param capacity 初始容量
     */
    public ResizableLinkedBlockingQueue(int capacity) {
        super(capacity);
        this.capacity = capacity;
    }
    
    /**
     * 设置新容量
     * @param newCapacity 新的队列容量
     */
    public synchronized void setCapacity(int newCapacity) {
        if (newCapacity <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than 0");
        }
        this.capacity = newCapacity;
    }
    
    /**
     * 获取当前容量
     * @return 当前容量
     */
    public int getCapacity() {
        return capacity;
    }
    
    /**
     * 获取剩余容量
     * 基于当前动态容量计算
     */
    @Override
    public int remainingCapacity() {
        return Math.max(0, capacity - size());
    }
    
    /**
     * 插入元素（非阻塞）
     * 如果队列已满（基于当前容量），返回 false
     */
    @Override
    public boolean offer(E e) {
        if (size() >= capacity) {
            return false;
        }
        return super.offer(e);
    }
    
    /**
     * 插入元素（超时）
     * 如果队列已满（基于当前容量），等待指定时间
     */
    @Override
    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        // 先检查容量
        if (size() >= capacity) {
            return false;
        }
        return super.offer(e, timeout, unit);
    }
    
    /**
     * 插入元素（阻塞）
     * 如果队列已满（基于当前容量），阻塞等待
     */
    @Override
    public void put(E e) throws InterruptedException {
        // 等待直到有空间
        while (size() >= capacity) {
            Thread.sleep(10);
        }
        super.put(e);
    }
    
    /**
     * 批量插入
     */
    @Override
    public boolean addAll(Collection<? extends E> c) {
        if (c == null) {
            throw new NullPointerException();
        }
        if (c == this) {
            throw new IllegalArgumentException();
        }
        
        // 检查容量是否足够
        if (size() + c.size() > capacity) {
            return false;
        }
        return super.addAll(c);
    }
}
