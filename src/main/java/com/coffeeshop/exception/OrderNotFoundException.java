package com.coffeeshop.exception;

/**
 * Exception thrown when an order is not found in the system.
 */
public class OrderNotFoundException extends BusinessLogicException {
    
    private static final long serialVersionUID = 1L;
    
    private static final String ERROR_CODE = "ORDER_NOT_FOUND";
    
    public OrderNotFoundException(String message) {
        super(message, ERROR_CODE);
    }
    
    public OrderNotFoundException(String message, Object rejectedValue) {
        super(message, ERROR_CODE, rejectedValue);
    }
    
    public OrderNotFoundException(String message, Throwable cause) {
        super(message, ERROR_CODE, cause);
    }
}