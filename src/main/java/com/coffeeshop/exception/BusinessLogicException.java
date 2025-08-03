package com.coffeeshop.exception;

/**
 * Base exception class for all business logic related exceptions in the coffee shop application.
 * This provides a common base for all custom business exceptions with consistent structure.
 */
public class BusinessLogicException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    private final String errorCode;
    private final Object rejectedValue;
    
    public BusinessLogicException(String message) {
        super(message);
        this.errorCode = null;
        this.rejectedValue = null;
    }
    
    public BusinessLogicException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.rejectedValue = null;
    }
    
    public BusinessLogicException(String message, String errorCode, Object rejectedValue) {
        super(message);
        this.errorCode = errorCode;
        this.rejectedValue = rejectedValue;
    }
    
    public BusinessLogicException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = null;
        this.rejectedValue = null;
    }
    
    public BusinessLogicException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.rejectedValue = null;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public Object getRejectedValue() {
        return rejectedValue;
    }
}