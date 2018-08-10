package com.testwa.core.base.exception;

/**
 * Created by wen on 19/10/2017.
 */
public class ReportException extends RuntimeException {

    public ReportException(String message){
        super(message);
    }

    public ReportException(String message, Throwable t){
        super(message, t);
    }
}
