package com.coffeeshop.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DTO chuẩn hóa phản hồi khi có lỗi (exception, validation, ...)
 * Dùng chung cho toàn bộ hệ thống
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponseDTO {
    private String message;      // Thông điệp lỗi (public, có thể show cho user)
    private String error;        // Tên lỗi/exception (ví dụ: "BadRequest", "ValidationError")
    private String path;         // API endpoint gây lỗi
    private String timestamp;    // Thời điểm lỗi (ISO format)
    private Integer status;      // HTTP status code (400, 404, 500...)

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