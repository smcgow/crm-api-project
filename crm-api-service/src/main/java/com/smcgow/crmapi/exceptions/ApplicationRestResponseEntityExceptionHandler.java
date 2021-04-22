package com.smcgow.crmapi.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class ApplicationRestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {


    @ExceptionHandler(value
            = { InvocationOfServiceFailedException.class, CrmApplicationConfigurationException.class
                ,RuntimeException.class,FailedAutheniticateRequestException.class,
    InvalidBasicAuthCredentialsException.class,JsonMessageParsingException.class,JsonResponseAbsentException.class})
    protected ResponseEntity<Object> handleConflict(
            RuntimeException ex, WebRequest request) {
        Map<String,String> errorResponse = new HashMap<>();
        errorResponse.put("code",HttpStatus.INTERNAL_SERVER_ERROR.toString());
        errorResponse.put("message",ex.getClass().getSimpleName() + " " +  ex.getMessage());
        errorResponse.put("timeStamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:s")));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    
}
