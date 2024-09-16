package io.github.kk01001.lock.exception;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Getter;

/**
 * @author kk01001
 * date 2022/2/22 17:10
 * 业务异常
 */
@Getter
public class LockException extends RuntimeException {

    private String requestId;

    public LockException(String requestId, String message) {
        super(message);
        this.requestId = requestId;
    }

    public LockException(String requestId, String message, Throwable e) {
        super(message, e);
        this.requestId = requestId;
    }

    public LockException(Throwable e) {
        super(ExceptionUtil.getMessage(e), e);
    }

    public LockException() {
    }

    public LockException(String message) {
        super(message);
    }

    public LockException(String messageTemplate, Object... params) {
        super(StrUtil.format(messageTemplate, params));
    }

    public LockException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public LockException(Throwable throwable, String messageTemplate, Object... params) {
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
