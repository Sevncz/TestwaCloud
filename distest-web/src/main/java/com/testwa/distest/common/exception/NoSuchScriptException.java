package com.testwa.distest.common.exception;

/**
 * Created by wen on 29/07/2017.
 */
public class NoSuchScriptException extends ObjectNotExistsException {

    public NoSuchScriptException() {
    }

    public NoSuchScriptException(String message) {
        super(message);
    }
}
