package io.github.kk01001.balancer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author kk01001
 * date 2021/9/26 12:54
 * 随机
 */
@Component
@Slf4j
public class RandomLoadBalancer<T> implements LoadBalancer<T> {

    @Override
    public T get(String key, List<T> dataList) {
        return getNum(dataList);
    }

    @Override
    public T get(List<T> dataList) {
        return getNum(dataList);
    }

    private T getNum(List<T> dataList) {
        int ind = ThreadLocalRandom.current().nextInt(dataList.size());
        return dataList.get(ind);
    }
}
