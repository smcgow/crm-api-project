package com.smcgow.crmapi.exceptions;

public class JsonResponseAbsentException extends RuntimeException{

    public JsonResponseAbsentException(String message) {
        super(message);
    }

    public JsonResponseAbsentException(String message, Throwable cause) {
        super(message, cause);
    }

    public JsonResponseAbsentException(Throwable cause) {
        super(cause);
    }
}
