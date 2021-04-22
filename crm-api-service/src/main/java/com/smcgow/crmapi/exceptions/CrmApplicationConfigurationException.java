package com.smcgow.crmapi.exceptions;

public class CrmApplicationConfigurationException extends RuntimeException{

    public CrmApplicationConfigurationException(String message) {
        super(message);
    }

    public CrmApplicationConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public CrmApplicationConfigurationException(Throwable cause) {
        super(cause);
    }
}
