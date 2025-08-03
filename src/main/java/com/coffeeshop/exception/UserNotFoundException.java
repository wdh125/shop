package com.coffeeshop.exception;

/**
 * Exception thrown when a user is not found in the system.
 */
public class UserNotFoundException extends BusinessLogicException {
    
    private static final long serialVersionUID = 1L;
    private static final String ERROR_CODE = "USER_NOT_FOUND";
    
    public UserNotFoundException(String message) {
        super(message, ERROR_CODE);
    }
    
    public UserNotFoundException(String message, Object rejectedValue) {
        super(message, ERROR_CODE, rejectedValue);
    }
    
    public UserNotFoundException(String message, Throwable cause) {
        super(message, ERROR_CODE, cause);
    }
}