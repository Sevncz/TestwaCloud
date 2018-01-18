package com.testwa.core.base.exception;

/**
 * Created by wen on 19/10/2017.
 */
public class ObjectNotExistsException extends RuntimeException {

    public ObjectNotExistsException(String message){
        super(message);
    }

    public ObjectNotExistsException(String message, Throwable t){
        super(message, t);
    }
}
