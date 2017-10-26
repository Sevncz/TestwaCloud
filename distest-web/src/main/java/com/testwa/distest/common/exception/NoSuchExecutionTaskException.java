package com.testwa.distest.common.exception;

/**
 * Created by wen on 29/07/2017.
 */
public class NoSuchExecutionTaskException extends ObjectNotExistsException {

    public NoSuchExecutionTaskException() {
    }

    public NoSuchExecutionTaskException(String message) {
        super(message);
    }
}
