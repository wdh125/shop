package com.coffeeshop.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/**
 * Enhanced error response DTO specifically for validation errors.
 * Extends the basic error response with field-specific error details.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValidationErrorResponseDTO {
    private String message;
    private String error;
    private String path;
    private String timestamp;
    private Integer status;
    private List<FieldErrorDTO> fieldErrors;
    
    public ValidationErrorResponseDTO() {}

    public ValidationErrorResponseDTO(String message, String error, String path, String timestamp, Integer status) {
        this.message = message;
        this.error = error;
        this.path = path;
        this.timestamp = timestamp;
        this.status = status;
    }

    public ValidationErrorResponseDTO(String message, String error, String path, String timestamp, Integer status, List<FieldErrorDTO> fieldErrors) {
        this.message = message;
        this.error = error;
        this.path = path;
        this.timestamp = timestamp;
        this.status = status;
        this.fieldErrors = fieldErrors;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public List<FieldErrorDTO> getFieldErrors() {
        return fieldErrors;
    }

    public void setFieldErrors(List<FieldErrorDTO> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }
}