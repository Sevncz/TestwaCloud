package com.testwa.distest.server.exception;

/**
 * Created by wen on 29/07/2017.
 */
public class AuthorizedException extends Exception {

    public AuthorizedException() {
    }

    public AuthorizedException(String message) {
        super(message);
    }
}