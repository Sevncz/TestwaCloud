package com.testwa.core.base.exception;

/**
 * Created by wen on 19/10/2017.
 */
public class ParamsFormatException extends ParamsException {

    public ParamsFormatException(){
    }

    public ParamsFormatException(String message){
        super(message);
    }

    public ParamsFormatException(String message, Throwable t){
        super(message, t);
    }
}
