package com.testwa.core.base.mybatis.exception;

public class UncachedMapperException extends Exception {

    public UncachedMapperException() {
        super();
    }

    public UncachedMapperException(String message) {
        super(message);
    }

    public UncachedMapperException(String message, Throwable cause) {
        super(message, cause);
    }

    public UncachedMapperException(Throwable cause) {
        super(cause);
    }

    protected UncachedMapperException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}