package com.testwa.core.base.mybatis.exception;

public class LockerException extends RuntimeException {

    public LockerException() {}

    public LockerException(String message) {
        super(message);
    }

    public LockerException(String message, Throwable cause) {
        super(message, cause);
    }

    public LockerException(Throwable cause) {
        super(cause);
    }
}