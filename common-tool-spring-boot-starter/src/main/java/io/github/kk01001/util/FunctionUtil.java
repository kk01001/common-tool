package io.github.kk01001.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.function.ThrowingSupplier;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author linshiqiang
 * @date 2024-12-06 09:36:00
 * @description 一些函数式的通用用法
 */
@Slf4j
public class FunctionUtil {

    /**
     * 通用的缓存读取或加载逻辑
     *
     * @param cacheKey    缓存键
     * @param loaderCache 读缓存
     * @param loaderDb    数据库加载逻辑
     * @param consumer    写缓存
     * @param <T>         缓存对象类型
     * @return 返回缓存或数据库加载的结果
     */
    private <T> Optional<T> getCachedOrLoadDb(String cacheKey,
                                              ThrowingSupplier<Optional<T>> loaderCache,
                                              Supplier<Optional<T>> loaderDb,
                                              Consumer<T> consumer) {
        try {
            Optional<T> loaderCacheOptional = loaderCache.get();
            if (loaderCacheOptional.isPresent()) {
                return loaderCacheOptional;
            }
        } catch (Exception e) {
            log.error("[getCachedOrLoad] read cacheKey: {}, error: ", cacheKey, e);
        }

        // 从数据库加载数据
        Optional<T> valueOptional = loaderDb.get();
        if (valueOptional.isEmpty()) {
            return Optional.empty();
        }
        T value = valueOptional.get();
        // 将加载的数据存入缓存
        try {
            consumer.accept(value);
        } catch (Exception e) {
            log.error("[getCachedOrLoad] write cacheKey: {}, error: ", cacheKey, e);
        }
        return Optional.of(value);
    }
}
