package com.testwa.distest.client.exception;public class CommandFailureException extends Exception{    public CommandFailureException(){    }    public CommandFailureException(String message){        super(message);    }    public CommandFailureException(String message, Throwable e){        super(message, e);    }}