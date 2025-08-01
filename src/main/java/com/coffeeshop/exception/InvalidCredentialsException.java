package com.coffeeshop.exception;

/**
 * Exception thrown when authentication fails due to invalid credentials.
 */
public class InvalidCredentialsException extends BusinessLogicException {
    
    private static final String ERROR_CODE = "INVALID_CREDENTIALS";
    
    public InvalidCredentialsException(String message) {
        super(message, ERROR_CODE);
    }
    
    public InvalidCredentialsException(String message, Object rejectedValue) {
        super(message, ERROR_CODE, rejectedValue);
    }
    
    public InvalidCredentialsException(String message, Throwable cause) {
        super(message, ERROR_CODE, cause);
    }
}