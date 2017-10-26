package com.testwa.distest.common.exception;

/**
 * Created by wen on 29/07/2017.
 */
public class NoSuchProjectException extends ObjectNotExistsException {

    public NoSuchProjectException() {
    }

    public NoSuchProjectException(String message) {
        super(message);
    }
}
