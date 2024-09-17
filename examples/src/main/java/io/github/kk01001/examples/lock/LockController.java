package io.github.kk01001.examples.lock;

import io.github.kk01001.lock.aspect.Lock;
import io.github.kk01001.lock.enums.LockType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * @author linshiqiang
 * @date 2024-09-17 20:02:00
 * @description
 */
@RestController
@RequestMapping("lock")
public class LockController {

    @Lock(key = "'lock:redissonSemaphore:' + #id",
            type = LockType.REDISSON_SEMAPHORE,
            permits = 5,
            timeout = 3000)
    @PostMapping("redissonSemaphore")
    public void redissonSemaphore(String id) throws InterruptedException {
        TimeUnit.SECONDS.sleep(10);
    }
}
