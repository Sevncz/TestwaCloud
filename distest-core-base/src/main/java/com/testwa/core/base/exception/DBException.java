package com.testwa.core.base.exception;

/**
 * Created by wen on 19/10/2017.
 */
public class DBException extends RuntimeException {

    public DBException(String message){
        super(message);
    }

    public DBException(String message, Throwable t){
        super(message, t);
    }
}
