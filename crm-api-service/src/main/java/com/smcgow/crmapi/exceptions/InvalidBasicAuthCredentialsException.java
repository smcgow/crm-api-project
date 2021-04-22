package com.smcgow.crmapi.exceptions;

public class InvalidBasicAuthCredentialsException extends RuntimeException{

    public InvalidBasicAuthCredentialsException(String message) {
        super(message);
    }

    public InvalidBasicAuthCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidBasicAuthCredentialsException(Throwable cause) {
        super(cause);
    }
    
}
