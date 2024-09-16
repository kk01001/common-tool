# 分布式锁和单机锁

## 单机

- [x] ReentrantLock
- [x] Semaphore
- [ ] StampedLock

## 分布式

- [x] redisson RSemaphore
- [x] redisson RLock 常规锁, 公平锁, 自旋锁
- [ ] redis setNx
- [ ] 线程池
- [ ] 数据库
    - 主键插入获取锁, 删除释放锁
    - for update
- [ ] Zookeeper