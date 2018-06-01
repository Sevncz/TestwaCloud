package com.testwa.core.base.exception;

/**
 * Created by wen on 19/10/2017.
 */
public class AccountNoActiveException extends RuntimeException {

    public AccountNoActiveException(String message){
        super(message);
    }

    public AccountNoActiveException(String message, Throwable t){
        super(message, t);
    }
}
