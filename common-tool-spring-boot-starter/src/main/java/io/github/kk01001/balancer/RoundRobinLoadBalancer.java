package io.github.kk01001.balancer;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author kk01001
 * date 2021/9/26 10:11
 * 轮训
 */
@Component
@Slf4j
public class RoundRobinLoadBalancer<T> implements LoadBalancer<T> {

    private static final ConcurrentHashMap<String, AtomicInteger> INDEX_MAP = new ConcurrentHashMap<>(32);

    private final AtomicInteger count;

    public RoundRobinLoadBalancer() {
        count = new AtomicInteger(0);
    }

    public static void clear() {
        INDEX_MAP.clear();
    }

    @Override
    public T get(String key, List<T> dataList) {
        if (CollUtil.isEmpty(dataList)) {
            return null;
        }
        try {
            AtomicInteger currentCount = INDEX_MAP.getOrDefault(key, INDEX_MAP.putIfAbsent(key, new AtomicInteger(0)));
            int index = incrementAndGetModulo(dataList.size(), currentCount);
            return dataList.get(index);
        } catch (Exception e) {
            log.error("get RoundRobinLoadBalancer error, ", e);
            INDEX_MAP.remove(key);
        }
        return null;
    }

    @Override
    public T get(List<T> dataList) {
        if (CollUtil.isEmpty(dataList)) {
            return null;
        }
        try {
            int index = incrementAndGetModulo(dataList.size(), count);
            return dataList.get(index);
        } catch (Exception e) {
            log.error("get RoundRobinLoadBalancer error, ", e);
            count.set(0);
        }
        return null;
    }

    private int incrementAndGetModulo(int modulo, AtomicInteger count) {
        for (; ; ) {
            int current = count.get();
            int next = (current + 1) % modulo;
            if (count.compareAndSet(current, next)) {
                return next;
            }
        }
    }

}
