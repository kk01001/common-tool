package io.github.kk01001.exception;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;

/**
 * @author kk01001
 * date 2022/2/22 17:10
 * 参数异常
 */
public class ParamsException extends RuntimeException {

    private String requestId;

    public ParamsException(String requestId, String message) {
        super(message);
        this.requestId = requestId;
    }

    public ParamsException(String requestId, String message, Throwable e) {
        super(message, e);
        this.requestId = requestId;
    }

    public ParamsException(Throwable e) {
        super(ExceptionUtil.getMessage(e), e);
    }

    public ParamsException() {
    }

    public ParamsException(String message) {
        super(message);
    }

    public ParamsException(String messageTemplate, Object... params) {
        super(StrUtil.format(messageTemplate, params));
    }

    public ParamsException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public ParamsException(Throwable throwable, String messageTemplate, Object... params) {
        super(StrUtil.format(messageTemplate, params), throwable);
    }

    public String getRequestId() {
        return requestId;
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
