package com.coffeeshop.exception;

/**
 * Exception thrown when attempting to create a user that already exists.
 */
public class UserAlreadyExistsException extends BusinessLogicException {
    
    private static final String ERROR_CODE = "USER_ALREADY_EXISTS";
    
    public UserAlreadyExistsException(String message) {
        super(message, ERROR_CODE);
    }
    
    public UserAlreadyExistsException(String message, Object rejectedValue) {
        super(message, ERROR_CODE, rejectedValue);
    }
    
    public UserAlreadyExistsException(String message, Throwable cause) {
        super(message, ERROR_CODE, cause);
    }
}