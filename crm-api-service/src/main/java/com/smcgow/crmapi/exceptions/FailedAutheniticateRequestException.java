package com.smcgow.crmapi.exceptions;

public class FailedAutheniticateRequestException extends RuntimeException {

    public FailedAutheniticateRequestException(String message) {
        super(message);
    }

    public FailedAutheniticateRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public FailedAutheniticateRequestException(Throwable cause) {
        super(cause);
    }
}
