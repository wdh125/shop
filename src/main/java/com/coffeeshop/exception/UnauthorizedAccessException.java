package com.coffeeshop.exception;

/**
 * Exception thrown when a user attempts to access resources they don't have permission for.
 */
public class UnauthorizedAccessException extends BusinessLogicException {
    
    private static final long serialVersionUID = 1L;
    private static final String ERROR_CODE = "UNAUTHORIZED_ACCESS";
    
    public UnauthorizedAccessException(String message) {
        super(message, ERROR_CODE);
    }
    
    public UnauthorizedAccessException(String message, Object rejectedValue) {
        super(message, ERROR_CODE, rejectedValue);
    }
    
    public UnauthorizedAccessException(String message, Throwable cause) {
        super(message, ERROR_CODE, cause);
    }
}