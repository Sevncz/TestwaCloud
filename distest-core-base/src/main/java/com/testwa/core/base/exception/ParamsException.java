package com.testwa.core.base.exception;

/**
 * Created by wen on 19/10/2017.
 */
public class ParamsException extends RuntimeException {

    public ParamsException(String message){
        super(message);
    }

    public ParamsException(String message, Throwable t){
        super(message, t);
    }
}
