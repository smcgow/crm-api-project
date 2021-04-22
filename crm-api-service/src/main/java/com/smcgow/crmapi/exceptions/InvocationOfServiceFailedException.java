package com.smcgow.crmapi.exceptions;

public class InvocationOfServiceFailedException extends RuntimeException {

    public InvocationOfServiceFailedException(String message) {
        super(message);
    }

    public InvocationOfServiceFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvocationOfServiceFailedException(Throwable cause) {
        super(cause);
    }
}
