package com.testwa.distest.client.component.logcat;

public class LogParsingException extends Exception {


    public LogParsingException(String message) {
        super(message);
    }

    public LogParsingException(String message, Throwable cause) {
        super(message, cause);
    }

    public LogParsingException(Throwable cause) {
        super(cause);
    }

}