package com.testwa.core.base.exception;

/**
 * Created by wen on 29/07/2017.
 */
public class AuthorizedException extends RuntimeException {

    public AuthorizedException(String message){
        super(message);
    }

    public AuthorizedException(String message, Throwable t){
        super(message, t);
    }
}
