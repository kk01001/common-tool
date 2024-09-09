package io.github.kk01001.ratelimter.exception;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Getter;

/**
 * @author kk01001
 * date 2022/2/22 17:10
 * 业务异常
 */
@Getter
public class RateLimitException extends RuntimeException {

    private String requestId;

    public RateLimitException(String requestId, String message) {
        super(message);
        this.requestId = requestId;
    }

    public RateLimitException(String requestId, String message, Throwable e) {
        super(message, e);
        this.requestId = requestId;
    }

    public RateLimitException(Throwable e) {
        super(ExceptionUtil.getMessage(e), e);
    }

    public RateLimitException() {
    }

    public RateLimitException(String message) {
        super(message);
    }

    public RateLimitException(String messageTemplate, Object... params) {
        super(StrUtil.format(messageTemplate, params));
    }

    public RateLimitException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public RateLimitException(Throwable throwable, String messageTemplate, Object... params) {
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
