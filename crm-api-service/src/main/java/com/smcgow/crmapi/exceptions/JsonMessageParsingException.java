package com.smcgow.crmapi.exceptions;

public class JsonMessageParsingException extends RuntimeException {

    public JsonMessageParsingException(String message) {
        super(message);
    }

    public JsonMessageParsingException(String message, Throwable cause) {
        super(message, cause);
    }

    public JsonMessageParsingException(Throwable cause) {
        super(cause);
    }
}
