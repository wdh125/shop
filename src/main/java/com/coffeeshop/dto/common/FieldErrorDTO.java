package com.coffeeshop.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DTO for field-specific error information in validation errors.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FieldErrorDTO {
    private String field;
    private Object rejectedValue;
    private String message;
    private String errorCode;

    public FieldErrorDTO() {}

    public FieldErrorDTO(String field, Object rejectedValue, String message) {
        this.field = field;
        this.rejectedValue = rejectedValue;
        this.message = message;
    }

    public FieldErrorDTO(String field, Object rejectedValue, String message, String errorCode) {
        this.field = field;
        this.rejectedValue = rejectedValue;
        this.message = message;
        this.errorCode = errorCode;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public Object getRejectedValue() {
        return rejectedValue;
    }

    public void setRejectedValue(Object rejectedValue) {
        this.rejectedValue = rejectedValue;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
}