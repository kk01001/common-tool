package io.github.kk01001.id;

import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author linshiqiang
 * date:  2024-07-02 17:21
 */
@Slf4j
public class IdWorkerUtil {

    private volatile IdWorker idWorker;

    private final ReentrantLock lock = new ReentrantLock();

    private final Long workerId;

    public IdWorkerUtil(Long workerId) {
        this.workerId = workerId;
        buildIdWorker();
    }

    public void buildIdWorker() {
        lock.lock();
        try {
            if (Objects.nonNull(this.idWorker)) {
                return;
            }
            this.idWorker = new IdWorker(this.workerId);
        } finally {
            lock.unlock();
        }
    }

    public IdWorker buildIdWorker(Long workerId) {
        lock.lock();
        try {
            return new IdWorker(workerId);
        } finally {
            lock.unlock();
        }
    }

    public Long nextId() {
        if (Objects.nonNull(this.idWorker)) {
            buildIdWorker(this.workerId);
        }
        return this.idWorker.nextId();
    }

    public String nextIdStr() {
        return String.valueOf(nextId());
    }
}
