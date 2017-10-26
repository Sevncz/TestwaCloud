package com.testwa.distest.common.exception;

/**
 * Created by wen on 29/07/2017.
 */
public class NoSuchTestcaseException extends ObjectNotExistsException {

    public NoSuchTestcaseException() {
    }

    public NoSuchTestcaseException(String message) {
        super(message);
    }
}
