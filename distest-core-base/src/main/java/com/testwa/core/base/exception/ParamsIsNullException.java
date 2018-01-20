package com.testwa.core.base.exception;

/**
 * Created by wen on 19/10/2017.
 */
public class ParamsIsNullException extends ParamsException {

    public ParamsIsNullException(){
    }

    public ParamsIsNullException(String message){
        super(message);
    }

    public ParamsIsNullException(String message, Throwable t){
        super(message, t);
    }
}
