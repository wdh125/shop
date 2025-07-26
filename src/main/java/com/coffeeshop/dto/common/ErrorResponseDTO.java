package com.coffeeshop.dto.common;

public class ErrorResponseDTO {
    private String message;
    private String error;
    private String path;
    private String timestamp;
    private Integer status;

    public ErrorResponseDTO() {}
    
    public ErrorResponseDTO(String message) { 
        this.message = message; 
    }
    
    public ErrorResponseDTO(String message, String error, String path, String timestamp, Integer status) {
        this.message = message;
        this.error = error;
        this.path = path;
        this.timestamp = timestamp;
        this.status = status;
    }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
} 