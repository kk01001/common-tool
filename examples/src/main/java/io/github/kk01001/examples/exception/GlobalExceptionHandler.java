package io.github.kk01001.examples.exception;

import io.github.kk01001.common.model.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author linshiqiang
 * @date 2024-12-03 14:11:00
 * @description
 */
@Slf4j
@RestControllerAdvice()
@RequiredArgsConstructor
public class GlobalExceptionHandler implements Ordered {

    /**
     * 其他全部异常
     *
     * @param e 全局异常
     * @return 错误结果
     */
    @ExceptionHandler(Exception.class)
    public ApiResponse<?> errorHandler(Exception e) {
        log.error(e.getMessage(), e);
        return ApiResponse.failOfMessage(e.getMessage(), 500);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
