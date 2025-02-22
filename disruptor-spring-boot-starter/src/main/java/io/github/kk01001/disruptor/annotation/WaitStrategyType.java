package io.github.kk01001.disruptor.annotation;

import com.lmax.disruptor.*;

import java.util.concurrent.TimeUnit;

/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description Disruptor等待策略枚举类,用于配置消费者如何等待生产者
 */
public enum WaitStrategyType {

    /**
     * 阻塞等待策略 使用锁和条件变量来实现等待 当没有可消费的事件时会阻塞线程 适用于低延迟但对CPU资源不敏感的场景
     */
    BLOCKING {
        @Override
        public WaitStrategy create() {
            return new BlockingWaitStrategy();
        }
    },
    /**
     * 让出CPU等待策略 使用Thread.yield()让出CPU 在等待时会不断循环并让出CPU直到有新事件 适用于低延迟且CPU资源充足的场景
     */
    YIELDING {
        @Override
        public WaitStrategy create() {
            return new YieldingWaitStrategy();
        }
    },
    /**
     * 自旋等待策略 使用自旋锁实现等待 在等待时会占用CPU不断自旋直到有新事件 适用于超低延迟但CPU资源充足的场景
     */
    BUSY_SPIN {
        @Override
        public WaitStrategy create() {
            return new BusySpinWaitStrategy();
        }
    },
    /**
     * 休眠等待策略 在等待时会短暂休眠让出CPU 适用于对延迟不敏感但需要节省CPU资源的场景
     */
    SLEEPING {
        @Override
        public WaitStrategy create() {
            return new SleepingWaitStrategy();
        }
    },
    /**
     * 带超时的阻塞等待策略 在指定时间内没有数据会抛出超时异常 适用于需要超时控制的场景
     */
    TIMEOUT_BLOCKING {
        @Override
        public WaitStrategy create() {
            // 默认1秒超时
            return new TimeoutBlockingWaitStrategy(1000, TimeUnit.MILLISECONDS);
        }
    },
    /**
     * 轻量级阻塞等待策略 相比BlockingWaitStrategy实现更简单 性能略好但功能更基础 适用于一般性能场景
     */
    LITE_BLOCKING {
        @Override
        public WaitStrategy create() {
            return new LiteBlockingWaitStrategy();
        }
    },
    /**
     * 分阶段回退等待策略 结合了多种等待策略: 1. 先自旋一定次数 2. 然后使用Thread.yield()让出CPU 3. 最后使用阻塞策略
     * 适用于需要平衡延迟和CPU使用的场景
     */
    PHASED_BACKOFF {
        @Override
        public WaitStrategy create() {
            // 默认配置:
            // - 自旋100次
            // - 等待1000纳秒
            // - 使用LiteBlockingWaitStrategy作为回退策略
            return new PhasedBackoffWaitStrategy(
                    100,
                    1000,
                    TimeUnit.NANOSECONDS,
                    new LiteBlockingWaitStrategy()
            );
        }
    };

    /**
     * 创建对应的等待策略实例
     *
     * @return 等待策略实例
     */
    public abstract WaitStrategy create();
}
