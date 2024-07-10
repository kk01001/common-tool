package io.github.kk01001.util;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

/**
 * @author kk01001
 * date 2023-06-18 19:42:00
 */
@Slf4j
public class Utils {

    /**
     * 格式化文本，使用 ${varName} 占位<br>
     * map = {a: "aValue", b: "bValue"} format("${a} and ${b}", map) ---=》 aValue and bValue
     *
     * @param template   文本模板，被替换的部分用 ${key} 表示
     * @param map        参数值对
     * @param ignoreNull 是否忽略 {@code null} 值，忽略则 {@code null} 值对应的变量不被替换，否则替换为""
     * @return 格式化后的文本
     * @since 5.7.10
     */
    public static String format(CharSequence template, Map<?, ?> map, boolean ignoreNull) {
        if (null == template) {
            return null;
        }
        if (null == map || map.isEmpty()) {
            return template.toString();
        }

        String template2 = template.toString();
        String value;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            value = StrUtil.utf8Str(entry.getValue());
            if (null == value && ignoreNull) {
                continue;
            }
            template2 = StrUtil.replace(template2, "${" + entry.getKey() + "}", value);
        }
        return template2;
    }

    /**
     * 分片
     */
    public static <T> List<T> getShardList(List<T> list, int shardTotal, int shardIndex) {
        List<T> shardList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            if (i % shardTotal == shardIndex) {
                shardList.add(list.get(i));
            }
        }
        if (shardIndex == -1) {
            shardList = new ArrayList<>();
        }
        return shardList;
    }

    public static <T> void consumerParallel(List<T> list, ExecutorService executorService, Consumer<T> consumer) {
        CountDownLatch latch = new CountDownLatch(list.size());
        for (T t : list) {
            execute(() -> {
                consumer.accept(t);
                latch.countDown();
            }, executorService);
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            log.error("latch.await error: ", e);
        }
    }

    public static void execute(Runnable runnable, ExecutorService executorService) {
        Map<String, String> copyOfContextMap = MDC.getCopyOfContextMap();
        executorService.execute(() -> {
            try {
                MDC.setContextMap(copyOfContextMap);
                runnable.run();
            } finally {
                MDC.clear();
            }
        });
    }

    public static void execute(Runnable runnable, Executor executorService) {
        Map<String, String> copyOfContextMap = MDC.getCopyOfContextMap();
        executorService.execute(() -> {
            try {
                MDC.setContextMap(copyOfContextMap);
                runnable.run();
            } finally {
                MDC.clear();
            }
        });
    }

    public static String toJson(ObjectMapper objectMapper, Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("toJson error: ", e);
        }
        return "";
    }

    public static <T> T toObject(ObjectMapper objectMapper, String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("toObject error: ", e);
        }
        return null;
    }

}
