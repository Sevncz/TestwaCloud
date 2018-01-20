package com.testwa.core.base.exception;

/**
 * Created by wen on 19/10/2017.
 */
public class AccountException extends RuntimeException {

    public AccountException(String message){
        super(message);
    }

    public AccountException(String message, Throwable t){
        super(message, t);
    }
}
