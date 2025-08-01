package com.coffeeshop.exception;

/**
 * Exception thrown when validation fails for business logic rules.
 */
public class ValidationException extends BusinessLogicException {
    
    private static final String ERROR_CODE = "VALIDATION_ERROR";
    
    public ValidationException(String message) {
        super(message, ERROR_CODE);
    }
    
    public ValidationException(String message, Object rejectedValue) {
        super(message, ERROR_CODE, rejectedValue);
    }
    
    public ValidationException(String message, Throwable cause) {
        super(message, ERROR_CODE, cause);
    }
}