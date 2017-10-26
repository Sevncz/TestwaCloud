package com.testwa.distest.common.exception;

/**
 * Created by wen on 29/07/2017.
 */
public class NoSuchTaskException extends ObjectNotExistsException {

    public NoSuchTaskException() {
    }

    public NoSuchTaskException(String message) {
        super(message);
    }
}
