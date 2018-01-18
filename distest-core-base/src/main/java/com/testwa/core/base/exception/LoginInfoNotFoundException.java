package com.testwa.core.base.exception;

/**
 * Created by wen on 29/07/2017.
 */
public class LoginInfoNotFoundException extends RuntimeException {

    public LoginInfoNotFoundException(String message){
        super(message);
    }

    public LoginInfoNotFoundException(String message, Throwable t){
        super(message, t);
    }
}
