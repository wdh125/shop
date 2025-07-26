package com.coffeeshop.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DTO trả về message thành công, thông báo, v.v.
 * Dùng chung cho toàn bộ hệ thống
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageResponseDTO {
    private String message;      // Nội dung message (có thể là "Thành công", "Sửa thành công")
    private String type;         // Loại message ("success", "info", "warning", "error")
    private String code;         // Mã message cho đa ngôn ngữ (ví dụ: "SUCCESS", "ORDER_NOT_FOUND")
    private String timestamp;    // Thời điểm gửi message (ISO format)

    public MessageResponseDTO() {}
    
    public MessageResponseDTO(String message) { 
        this.message = message; 
    }
    
    public MessageResponseDTO(String message, String type) {
        this.message = message;
        this.type = type;
    }
    
    public MessageResponseDTO(String message, String type, String code) {
        this.message = message;
        this.type = type;
        this.code = code;
    }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
} 