package com.coffeeshop.exception;

/**
 * Exception thrown when a product is not found in the system.
 */
public class ProductNotFoundException extends BusinessLogicException {
    
    private static final long serialVersionUID = 1L;
    private static final String ERROR_CODE = "PRODUCT_NOT_FOUND";
    
    public ProductNotFoundException(String message) {
        super(message, ERROR_CODE);
    }
    
    public ProductNotFoundException(String message, Object rejectedValue) {
        super(message, ERROR_CODE, rejectedValue);
    }
    
    public ProductNotFoundException(String message, Throwable cause) {
        super(message, ERROR_CODE, cause);
    }
}