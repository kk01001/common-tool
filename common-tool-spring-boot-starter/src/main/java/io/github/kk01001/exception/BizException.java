package io.github.kk01001.exception;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Getter;

/**
 * @author kk01001
 * date 2022/2/22 17:10
 * 业务异常
 */
@Getter
public class BizException extends RuntimeException {

    private String requestId;

    public BizException(String requestId, String message) {
        super(message);
        this.requestId = requestId;
    }

    public BizException(String requestId, String message, Throwable e) {
        super(message, e);
        this.requestId = requestId;
    }

    public BizException(Throwable e) {
        super(ExceptionUtil.getMessage(e), e);
    }

    public BizException() {
    }

    public BizException(String message) {
        super(message);
    }

    public BizException(String messageTemplate, Object... params) {
        super(StrUtil.format(messageTemplate, params));
    }

    public BizException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public BizException(Throwable throwable, String messageTemplate, Object... params) {
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
