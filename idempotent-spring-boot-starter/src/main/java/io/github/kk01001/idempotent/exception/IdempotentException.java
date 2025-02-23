package io.github.kk01001.idempotent.exception;

import lombok.Getter;

/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description 幂等异常
 */
@Getter
public class IdempotentException extends RuntimeException {

    private final String requestId;
    private final Object[] args;

    public IdempotentException(String message) {
        super(message);
        this.requestId = null;
        this.args = null;
    }

    public IdempotentException(String requestId, String message) {
        super(message);
        this.requestId = requestId;
        this.args = null;
    }

    public IdempotentException(String message, Object... args) {
        super(message);
        this.requestId = null;
        this.args = args;
    }

    public IdempotentException(String requestId, String message, Object... args) {
        super(message);
        this.requestId = requestId;
        this.args = args;
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
