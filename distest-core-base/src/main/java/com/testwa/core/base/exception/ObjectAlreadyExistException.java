package com.testwa.core.base.exception;

/**
 * Created by wen on 19/10/2017.
 */
public class ObjectAlreadyExistException extends RuntimeException {

    public ObjectAlreadyExistException(String message){

    }

    public ObjectAlreadyExistException(String message, Throwable t){
        super(message, t);
    }
}
