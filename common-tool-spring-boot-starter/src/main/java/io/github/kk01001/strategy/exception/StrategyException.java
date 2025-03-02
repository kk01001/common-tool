package io.github.kk01001.strategy.exception;

import lombok.Getter;

/**
 * 策略模式异常类
 */
@Getter
public class StrategyException extends RuntimeException {

    private final String strategyType;
    private final String errorCode;

    public StrategyException(String message) {
        super(message);
        this.strategyType = null;
        this.errorCode = null;
    }

    public StrategyException(String message, String strategyType) {
        super(message);
        this.strategyType = strategyType;
        this.errorCode = null;
    }

    public StrategyException(String message, String strategyType, String errorCode) {
        super(message);
        this.strategyType = strategyType;
        this.errorCode = errorCode;
    }

    public StrategyException(String message, Throwable cause) {
        super(message, cause);
        this.strategyType = null;
        this.errorCode = null;
    }

    public StrategyException(String message, String strategyType, Throwable cause) {
        super(message, cause);
        this.strategyType = strategyType;
        this.errorCode = null;
    }

    public StrategyException(String message, String strategyType, String errorCode, Throwable cause) {
        super(message, cause);
        this.strategyType = strategyType;
        this.errorCode = errorCode;
    }

}