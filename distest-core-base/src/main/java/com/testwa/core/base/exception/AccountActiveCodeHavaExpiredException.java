package com.testwa.core.base.exception;

/**
 * Created by wen on 19/10/2017.
 */
public class AccountActiveCodeHavaExpiredException extends RuntimeException {

    public AccountActiveCodeHavaExpiredException(String message){
        super(message);
    }

    public AccountActiveCodeHavaExpiredException(String message, Throwable t){
        super(message, t);
    }
}
