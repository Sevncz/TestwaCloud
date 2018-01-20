package com.testwa.core.base.exception;

/**
 * Created by wen on 19/10/2017.
 */
public class AccountAlreadyExistException extends ObjectAlreadyExistException {

    public AccountAlreadyExistException(String message){
        super(message);
    }

    public AccountAlreadyExistException(String message, Throwable t){
        super(message, t);
    }
}
