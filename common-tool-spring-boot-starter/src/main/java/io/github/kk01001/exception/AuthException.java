package io.github.kk01001.exception;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Getter;

/**
 * @author kk01001
 * date 2022/2/22 17:10
 * 鉴权异常
 */
@Getter
public class AuthException extends RuntimeException {

    private String requestId;

    public AuthException(String requestId, String message) {
        super(message);
        this.requestId = requestId;
    }

    public AuthException(String requestId, String message, Throwable e) {
        super(message, e);
        this.requestId = requestId;
    }

    public AuthException(Throwable e) {
        super(ExceptionUtil.getMessage(e), e);
    }

    public AuthException() {
    }

    public AuthException(String message) {
        super(message);
    }

    public AuthException(String messageTemplate, Object... params) {
        super(StrUtil.format(messageTemplate, params));
    }

    public AuthException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public AuthException(Throwable throwable, String messageTemplate, Object... params) {
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
