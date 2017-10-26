package com.testwa.distest.common.exception;

/**
 * Created by wen on 29/07/2017.
 */
public class NotInProjectException extends ObjectNotExistsException {

    public NotInProjectException() {
    }

    public NotInProjectException(String message) {
        super(message);
    }
}
