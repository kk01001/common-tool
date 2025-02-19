package io.github.kk01001.disruptor.annotation;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.YieldingWaitStrategy;

public enum WaitStrategyType {
    BLOCKING {
        @Override
        public WaitStrategy create() {
            return new BlockingWaitStrategy();
        }
    },
    YIELDING {
        @Override
        public WaitStrategy create() {
            return new YieldingWaitStrategy();
        }
    },
    BUSY_SPIN {
        @Override
        public WaitStrategy create() {
            return new BusySpinWaitStrategy();
        }
    },
    SLEEPING {
        @Override
        public WaitStrategy create() {
            return new SleepingWaitStrategy();
        }
    };

    public abstract WaitStrategy create();
} 