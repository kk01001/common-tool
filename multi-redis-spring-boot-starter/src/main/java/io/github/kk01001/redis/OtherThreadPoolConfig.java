package io.github.kk01001.redis;

import com.alibaba.ttl.threadpool.TtlExecutors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author kk01001
 * @since 2021/9/10 11:39
 * 线程池配置
 */
@Slf4j
@Configuration
public class OtherThreadPoolConfig {

    public static final int CPU_NUM = Runtime.getRuntime().availableProcessors();

    public static final int QUEUE_CAPACITY = 20000;

    public static final String BACK_REDIS_POOL = "back-redis-pool-";

    /**
     * 异地机房redis操作
     */
    @Bean("otherRoomExecutor")
    public ExecutorService otherExecutor() {
        log.info("初始化 {} 线程池, 当前核数: {}", BACK_REDIS_POOL, CPU_NUM);
        ExecutorService executorService = new ThreadPoolExecutor(CPU_NUM,
                CPU_NUM,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(200),
                Thread.ofVirtual().name(BACK_REDIS_POOL, 0).factory(),
                new ThreadPoolExecutor.CallerRunsPolicy());
        return TtlExecutors.getTtlExecutorService(executorService);
    }

}
