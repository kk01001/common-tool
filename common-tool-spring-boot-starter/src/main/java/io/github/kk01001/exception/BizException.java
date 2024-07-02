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

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
