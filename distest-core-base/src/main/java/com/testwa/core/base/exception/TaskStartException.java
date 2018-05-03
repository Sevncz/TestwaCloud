package com.testwa.core.base.exception;

/**
 * Created by wen on 19/10/2017.
 */
public class TaskStartException extends RuntimeException {

    public TaskStartException(String message){

    }

    public TaskStartException(String message, Throwable t){
        super(message, t);
    }
}
