package com.coffeeshop.exception;

/**
 * Exception thrown when a reservation is not found in the system.
 */
public class ReservationNotFoundException extends BusinessLogicException {
    
    private static final String ERROR_CODE = "RESERVATION_NOT_FOUND";
    
    public ReservationNotFoundException(String message) {
        super(message, ERROR_CODE);
    }
    
    public ReservationNotFoundException(String message, Object rejectedValue) {
        super(message, ERROR_CODE, rejectedValue);
    }
    
    public ReservationNotFoundException(String message, Throwable cause) {
        super(message, ERROR_CODE, cause);
    }
}