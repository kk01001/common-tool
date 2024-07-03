package io.github.kk01001.exception;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Getter;

/**
 * @author kk01001
 * date 2022/2/22 17:10
 * 底层业务请求异常
 */
@Getter
public class BaseRequestException extends RuntimeException {

    public BaseRequestException(Throwable e) {
        super(ExceptionUtil.getMessage(e), e);
    }

    public BaseRequestException() {
    }

    public BaseRequestException(String message) {
        super(message);
    }

    public BaseRequestException(String messageTemplate, Object... params) {
        super(StrUtil.format(messageTemplate, params));
    }

    public BaseRequestException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public BaseRequestException(Throwable throwable, String messageTemplate, Object... params) {
        super(StrUtil.format(messageTemplate, params), throwable);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
