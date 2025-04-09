package io.github.kk01001.design.pattern.statemachine.exception;

/**
 * @author kk01001
 * @date 2024-02-13 14:31:00
 * @description 状态持久化异常
 */
public class StatePersistException extends RuntimeException {

    public StatePersistException(String message) {
        super(message);
    }

    public StatePersistException(String message, Throwable cause) {
        super(message, cause);
    }
}
