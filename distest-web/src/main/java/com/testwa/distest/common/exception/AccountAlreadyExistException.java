package com.testwa.distest.common.exception;

/**
 * Created by wen on 19/10/2017.
 */
public class AccountAlreadyExistException extends ObjectAlreadyExistException {

    public AccountAlreadyExistException(){

    }

    public AccountAlreadyExistException(String message){
        super(message);
    }
}
