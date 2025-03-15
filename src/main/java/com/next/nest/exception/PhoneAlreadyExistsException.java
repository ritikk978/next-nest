package com.next.nest.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class PhoneAlreadyExistsException extends RuntimeException {
    
    public PhoneAlreadyExistsException(String message) {
        super(message);
    }
    
    public PhoneAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}