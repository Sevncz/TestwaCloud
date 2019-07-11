package com.testwa.distest.client.support;

public class HttpRequestException extends RuntimeException {

    private static final long serialVersionUID = 8477965642656299750L;

    public HttpRequestException(String message) {
        super(message);
    }

    public HttpRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public HttpRequestException(Throwable cause) {
        super(cause);
    }
}
