package io.github.kk01001.resilience4j.exception;

/**
 * Resilience4j 异常
 *
 * @author kk01001
 */
public class ResilienceException extends RuntimeException {

    public ResilienceException(String message) {
        super(message);
    }

    public ResilienceException(String message, Throwable cause) {
        super(message, cause);
    }
}
