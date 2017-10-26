package com.testwa.distest.common.exception;

/**
 * Created by wen on 29/07/2017.
 */
public class NoSuchAppException extends ObjectNotExistsException {

    public NoSuchAppException() {
    }

    public NoSuchAppException(String message) {
        super(message);
    }
}
