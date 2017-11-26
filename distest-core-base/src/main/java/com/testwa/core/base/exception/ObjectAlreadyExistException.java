package com.testwa.core.base.exception;

/**
 * Created by wen on 19/10/2017.
 */
public class ObjectAlreadyExistException extends Exception {

    public ObjectAlreadyExistException(){

    }

    public ObjectAlreadyExistException(String message){
        super(message);
    }
}
